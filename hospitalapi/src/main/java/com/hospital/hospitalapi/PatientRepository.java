package com.hospital.hospitalapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.name)          LIKE %:q% OR " +
           "LOWER(p.doctor)        LIKE %:q% OR " +
           "LOWER(p.hospital)      LIKE %:q% OR " +
           "LOWER(p.status)        LIKE %:q% OR " +
           "LOWER(p.conditionDesc) LIKE %:q% OR " +
           "CAST(p.age AS string)  LIKE %:q% OR " +
           "CAST(p.patientId AS string) LIKE %:q%")
    List<Patient> searchAllFields(@Param("q") String q);
}