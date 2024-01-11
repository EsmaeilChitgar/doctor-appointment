package com.blubank.doctorappointment.repository;

import com.blubank.doctorappointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByOpenTimeDoctorId(Long doctorId);
    List<Appointment> findByPatientPhoneNumber(String phoneNumber);
}
