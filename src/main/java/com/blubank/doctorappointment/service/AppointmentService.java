package com.blubank.doctorappointment.service;

import com.blubank.doctorappointment.model.Appointment;
import com.blubank.doctorappointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByOpenTimeDoctorId(doctorId);
    }

    public List<Appointment> getPatientAppointments(String phoneNumber) {
        return appointmentRepository.findByPatientPhoneNumber(phoneNumber);
    }
}
