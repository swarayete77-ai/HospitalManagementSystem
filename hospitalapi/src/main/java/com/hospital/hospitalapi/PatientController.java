package com.hospital.hospitalapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientRepository repo;

    // GET all → http://localhost:8080/api/patients
    @GetMapping
    public List<Patient> getAll() {
        return repo.findAll();
    }

    // GET one → http://localhost:8080/api/patients/101
    @GetMapping("/{id}")
    public Patient getById(@PathVariable int id) {
        return repo.findById(id).orElse(null);
    }

    // GET search → http://localhost:8080/api/patients/search?q=Hope
    @GetMapping("/search")
    public List<Patient> search(@RequestParam String q) {
        return repo.searchAllFields(q.toLowerCase());
    }

    // POST register → body: JSON patient object
    @PostMapping
    public Patient add(@RequestBody Patient patient) {
        return repo.save(patient);
    }

    // PUT edit → http://localhost:8080/api/patients/101, body: updated JSON
    @PutMapping("/{id}")
    public Patient update(@PathVariable int id, @RequestBody Patient updated) {
        updated.setPatientId(id);
        return repo.save(updated);
    }

    // DELETE → http://localhost:8080/api/patients/101
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        repo.deleteById(id);
        return "Patient " + id + " deleted.";
    }

    // GET stats → http://localhost:8080/api/patients/stats
    @GetMapping("/stats")
    public java.util.Map<String, Long> stats() {
        List<Patient> all = repo.findAll();
        java.util.Map<String, Long> map = new java.util.LinkedHashMap<>();
        map.put("total", (long) all.size());
        map.put("inpatient",    all.stream().filter(p -> "Inpatient".equalsIgnoreCase(p.getStatus())).count());
        map.put("outpatient",   all.stream().filter(p -> "Outpatient".equalsIgnoreCase(p.getStatus())).count());
        map.put("observation",  all.stream().filter(p -> "Observation".equalsIgnoreCase(p.getStatus())).count());
        map.put("ambulatory",   all.stream().filter(p -> "Ambulatory".equalsIgnoreCase(p.getStatus())).count());
        return map;
    }
}