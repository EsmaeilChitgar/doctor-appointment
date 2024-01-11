package com.blubank.doctorappointment.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "open_time_id")
    private OpenTime openTime;
}
