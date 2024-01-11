package com.blubank.doctorappointment.controller;

import com.blubank.doctorappointment.exception.AppointmentExistsException;
import com.blubank.doctorappointment.exception.OpenTimeNotFoundException;
import com.blubank.doctorappointment.exception.PatientNotFoundException;
import com.blubank.doctorappointment.model.Appointment;
import com.blubank.doctorappointment.model.Doctor;
import com.blubank.doctorappointment.model.OpenTime;
import com.blubank.doctorappointment.service.AppointmentService;
import com.blubank.doctorappointment.service.DoctorService;
import com.blubank.doctorappointment.service.OpenTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private OpenTimeService openTimeService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Doctor> addDoctor(@RequestBody Doctor doctor) {
        Doctor addedDoctor = doctorService.addDoctor(doctor);
        return new ResponseEntity<>(addedDoctor, HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<Object> getDoctor(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getDoctor(doctorId);
        return new ResponseEntity<>(doctor == null ? "No doctor found" : doctor, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Object> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return new ResponseEntity<>(doctors.isEmpty() ? "There is no doctor" : doctors, HttpStatus.OK);
    }

    @GetMapping("/{doctorId}/appointments")
    public ResponseEntity<Object> getDoctorAppointments(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId);
        return new ResponseEntity<>(appointments.isEmpty() ?
                "There is no appointment related to doctor" : appointments, HttpStatus.OK);
    }

    @GetMapping("/{doctorId}/opentimes")
    public ResponseEntity<Object> getAllOpenTimes(
            @PathVariable Long doctorId) {
        List<OpenTime> openTimes = openTimeService.getOpenTimes(doctorId);
        return new ResponseEntity<>(openTimes.isEmpty() ?
                "There is no open time related to doctor" : openTimes, HttpStatus.OK);
    }

    @PostMapping("/{doctorId}/add-opentimes")
    public ResponseEntity<Object> addOpenTimesForDoctor(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<OpenTime> openTimes = openTimeService.addOpenTimesForDoctor(doctorId, start, end);
            return new ResponseEntity<>(openTimes, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{doctorId}/delete-opentime/{openTimeId}")
    public ResponseEntity<Object> deleteOpenTimeForDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long openTimeId) {
        try {
            OpenTime openTime = openTimeService.deleteOpenTimeForDoctor(doctorId, openTimeId);
            return new ResponseEntity<>(openTime, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OpenTimeNotFoundException | PatientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AppointmentExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}