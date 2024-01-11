package com.blubank.doctorappointment.service;

import com.blubank.doctorappointment.model.Doctor;
import com.blubank.doctorappointment.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor addDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor getDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId).orElse(null);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}
