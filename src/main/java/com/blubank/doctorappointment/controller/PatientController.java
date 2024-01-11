package com.blubank.doctorappointment.controller;

import com.blubank.doctorappointment.exception.AppointmentExistsException;
import com.blubank.doctorappointment.exception.OpenTimeNotFoundException;
import com.blubank.doctorappointment.exception.PatientNotFoundException;
import com.blubank.doctorappointment.model.Appointment;
import com.blubank.doctorappointment.model.OpenTime;
import com.blubank.doctorappointment.model.Patient;
import com.blubank.doctorappointment.service.AppointmentService;
import com.blubank.doctorappointment.service.OpenTimeService;
import com.blubank.doctorappointment.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private OpenTimeService openTimeService;

    @PostMapping
    public ResponseEntity<Patient> addPatient(@RequestBody Patient patient) {
        Patient addedPatient = patientService.addPatient(patient);
        return new ResponseEntity<>(addedPatient, HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}/opentimes/{date}")
    public ResponseEntity<Object> getOpenTimesForDoctorOnDay(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<OpenTime> openTimes = openTimeService.getOpenTimesForDoctorOnDay(doctorId, date);
        return new ResponseEntity<>(openTimes.isEmpty() ?
                "There is no open time for the doctor in the specified date" : openTimes, HttpStatus.OK);
    }

    @PostMapping("/setAppointment/{openTimeId}")
    public ResponseEntity<Object> setAppointmentForPatient(
            @PathVariable Long openTimeId,
            @RequestParam String patientName,
            @RequestParam String phoneNumber) {
        try {
            Appointment appointment =
                    openTimeService.setAppointmentForPatient(patientName, phoneNumber, openTimeId);
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OpenTimeNotFoundException | PatientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AppointmentExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{phoneNumber}/appointments")
    public ResponseEntity<Object> getPatientAppointments(@PathVariable String phoneNumber) {
        List<Appointment> patientAppointments = appointmentService.getPatientAppointments(phoneNumber);
        return new ResponseEntity<>(patientAppointments.isEmpty() ?
                "There is no appointment for the patient" : patientAppointments, HttpStatus.OK);
    }
}