package com.blubank.doctorappointment.repository;

import com.blubank.doctorappointment.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByNameAndPhoneNumber(String name, String phoneNumber);
}
