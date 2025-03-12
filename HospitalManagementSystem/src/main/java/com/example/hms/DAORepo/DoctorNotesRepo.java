package com.example.hms.DAORepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DoctorNotesRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveNote(int doctorId, int patientId, String note) {
        String sql = "INSERT INTO doctor_notes (doctor_id, patient_id, note) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, doctorId, patientId, note);
    }
}