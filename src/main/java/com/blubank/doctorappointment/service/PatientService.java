package com.blubank.doctorappointment.service;

import com.blubank.doctorappointment.model.Patient;
import com.blubank.doctorappointment.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public Patient addPatient(Patient patient) {
        return patientRepository.save(patient);
    }
}
