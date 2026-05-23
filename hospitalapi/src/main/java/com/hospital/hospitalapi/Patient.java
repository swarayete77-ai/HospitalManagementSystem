package com.hospital.hospitalapi;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patients")   // must match your actual MySQL table name
@Data                        // Lombok generates getters/setters/toString for you
public class Patient {

    @Id
    @Column(name = "patient_id")   // adjust to match your exact column name
    private int patientId;

    @Column(name = "patient_name")
    private String name;

    @Column(name = "age")
    private int age;

    @Column(name = "doctor_name")
    private String doctor;

    @Column(name = "hospital_name")
    private String hospital;

    @Column(name = "status")
    private String status;

    @Column(name = "condition_desc")
    private String conditionDesc;
}