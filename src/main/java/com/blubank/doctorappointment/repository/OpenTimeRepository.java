package com.blubank.doctorappointment.repository;

import com.blubank.doctorappointment.model.Doctor;
import com.blubank.doctorappointment.model.OpenTime;
import com.blubank.doctorappointment.model.OpenTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OpenTimeRepository extends JpaRepository<OpenTime, Long> {
    List<OpenTime> findByDoctorAndStartTimeBetween(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime);
    List<OpenTime> findByDoctorAndStartTimeBetweenAndStatus(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime, OpenTimeStatus openTimeStatus);
    List<OpenTime> findAllByDoctor_Id(Long doctorId);
}
