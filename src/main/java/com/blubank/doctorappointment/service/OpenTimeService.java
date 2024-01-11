package com.blubank.doctorappointment.service;

import com.blubank.doctorappointment.exception.AppointmentExistsException;
import com.blubank.doctorappointment.exception.ConcurrencyException;
import com.blubank.doctorappointment.exception.OpenTimeNotFoundException;
import com.blubank.doctorappointment.exception.PatientNotFoundException;
import com.blubank.doctorappointment.helper.Util;
import com.blubank.doctorappointment.model.*;
import com.blubank.doctorappointment.repository.AppointmentRepository;
import com.blubank.doctorappointment.repository.DoctorRepository;
import com.blubank.doctorappointment.repository.OpenTimeRepository;
import com.blubank.doctorappointment.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.blubank.doctorappointment.helper.AppConstants.OPENTIME_INTERVAL_MINUTES;

@Service
public class OpenTimeService {
    @Autowired
    private OpenTimeRepository openTimeRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<OpenTime> getOpenTimesForDoctorOnDay(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        if (doctor != null) {
            LocalDateTime startDate = date.atStartOfDay();
            LocalDateTime endDate = date.plusDays(1).atStartOfDay();

            // note: returns all open-times status containing
            // {OpenTimeStatus.Open, OpenTimeStatus.Taken, OpenTimeStatus.Deleted}
            return openTimeRepository
                    .findByDoctorAndStartTimeBetweenAndStatus(doctor, startDate, endDate, OpenTimeStatus.OPEN);

            // note: returns just open-times status containing {OpenTimeStatus.Open}
            //return openTimeRepository.findByDoctorAndStartTimeBetween(doctor, startDate, endDate);
        }

        return List.of();
    }

    public List<OpenTime> getOpenTimes(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        if (doctor == null) {
            return List.of();
        }

        return openTimeRepository.findAllByDoctor_Id(doctorId);
    }

    public List<OpenTime> addOpenTimesForDoctor(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return null;
        }

        if (!endDateTime.toLocalDate().equals(startDateTime.toLocalDate())) {
            throw new IllegalArgumentException("Invalid time period: The date of start and end are not the same.");
        }

        if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("Invalid time period: End date is sooner than start date.");
        }

        if (!nextIntervalStartDateTime(startDateTime).isBefore(endDateTime.plusNanos(1))) {
            throw new IllegalArgumentException(
                    "Invalid time interval: the interval must be at least " + OPENTIME_INTERVAL_MINUTES +" minutes.");
        }

        List<OpenTime> openTimes = generateOpenTimes(startDateTime, endDateTime, doctor);
        openTimeRepository.saveAll(openTimes);
        return openTimes;
    }

    @Transactional
    public OpenTime deleteOpenTimeForDoctor(Long doctorId, Long openTimeId) {
        List<OpenTime> doctorOpenTimes = openTimeRepository.findAllByDoctor_Id(doctorId);
        if (doctorOpenTimes.isEmpty()) {
            throw new OpenTimeNotFoundException("Open time not found for doctor id " + doctorId);
        }

        if (doctorOpenTimes.stream().noneMatch(ot->ot.getId().equals(openTimeId))){
            throw new OpenTimeNotFoundException("Open time not related to doctor id " + doctorId);
        }

        OpenTime openTime = openTimeRepository.findById(openTimeId).orElse(null);
        if (openTime == null) {
            throw new OpenTimeNotFoundException("Open time not for openTimeId " + openTimeId);
        }

        if (openTime.getStatus().equals(OpenTimeStatus.TAKEN)) {
            throw new AppointmentExistsException("Cannot delete open time with existing appointments.");
        }

        if (openTime.getStatus().equals(OpenTimeStatus.OPEN)) {
            try {
                // note: Perform optimistic locking check by saving the entity
                openTimeRepository.delete(openTime);
            } catch (OptimisticLockException e) {
                throw new ConcurrencyException("Concurrency issue during open time deletion.");
            }
        }
        return openTime;
    }

    @Transactional
    public Appointment setAppointmentForPatient(Patient patient, Long openTimeId) {
        try {
            OpenTime openTime = openTimeRepository.findById(openTimeId).orElse(null);
            if (openTime == null) {
                throw new OpenTimeNotFoundException("Open time not for openTimeId " + openTimeId);
            }

            if (openTime.getStatus().equals(OpenTimeStatus.TAKEN)) {
                throw new AppointmentExistsException("Cannot pick taken open time with existing appointments.");
            }

            // note: in the current scenario, the OpenTimeStatus.DELETED never happens.
            // but maybe useful for the incoming scenarios.
            if (openTime.getStatus().equals(OpenTimeStatus.DELETED)) {
                throw new AppointmentExistsException("Cannot pick deleted open time.");
            }

            if (openTime.getStatus().equals(OpenTimeStatus.OPEN)) {
                openTime.setStatus(OpenTimeStatus.TAKEN);
                openTimeRepository.save(openTime);

                Appointment appointment = new Appointment();
                appointment.setPatient(patient);
                appointment.setOpenTime(openTime);
                return appointmentRepository.save(appointment);
            }
        } catch (OptimisticLockException e) {
            throw new OptimisticLockException("Concurrency issue during open time picking.");
        }

        return null;
    }

    @Transactional
    public Appointment setAppointmentForPatient(String patientName, String phoneNumber, Long openTimeId) {
        if (Util.isNullOrEmpty(patientName) || Util.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Both patient name and phone number are required.");
        }

        Patient patient = patientRepository.findByNameAndPhoneNumber(patientName, phoneNumber);

        if (patient != null) {
            return setAppointmentForPatient(patient, openTimeId);
        } else {
            throw new PatientNotFoundException("Patient not found.");
        }
    }

    private List<OpenTime> generateOpenTimes(LocalDateTime startDateTime, LocalDateTime endDateTime, Doctor doctor) {
        List<OpenTime> openTimes = new ArrayList<>();
        LocalDateTime currentDateTime = startDateTime;
        List<OpenTime> existingOpenTimes =
                openTimeRepository.findByDoctorAndStartTimeBetween(doctor, startDateTime, endDateTime);

        while (nextIntervalStartDateTime(currentDateTime).isBefore(endDateTime.plusNanos(1))) {
            OpenTime openTime = new OpenTime();
            openTime.setStartTime(currentDateTime);
            openTime.setEndTime(nextIntervalStartDateTime(currentDateTime));
            openTime.setDoctor(doctor);
            openTime.setStatus(OpenTimeStatus.OPEN);

            // note: avoid to add current date time to db, if there is a share between current date time and one of existing-db date-times
            if (!hasOverlap(openTime, existingOpenTimes)) {
                openTimes.add(openTime);
            }

            currentDateTime = nextIntervalStartDateTime(currentDateTime);
        }

        return openTimes;
    }

    private boolean hasOverlap(OpenTime newOpenTime, List<OpenTime> existingOpenTimes) {
        for (OpenTime existingOpenTime : existingOpenTimes) {
            if (existingOpenTime.getStartTime().isBefore(newOpenTime.getEndTime().minusNanos(1)) &&
                    existingOpenTime.getEndTime().isAfter(newOpenTime.getStartTime().plusNanos(1))) {
                return true;
            }
        }

        return false;
    }

    private LocalDateTime nextIntervalStartDateTime(LocalDateTime currentDateTime) {
        return currentDateTime.plusMinutes(OPENTIME_INTERVAL_MINUTES);
    }
}