package com.blubank.doctorappointment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class OpenTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private OpenTimeStatus status;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToOne(mappedBy = "openTime", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Appointment appointment;

    @Version
    private Long version;
}
