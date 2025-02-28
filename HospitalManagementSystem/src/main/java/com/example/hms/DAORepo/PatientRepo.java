package com.example.hms.DAORepo;

import com.example.hms.Model.Patient.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class PatientRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, contact_info) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(), patient.getGender(), patient.getContactInfo());
    }

    public Patient findByFirstNameAndLastName(String firstName, String lastName) {
        String sql = "SELECT * FROM patients WHERE first_name = ? AND last_name = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{firstName, lastName}, new PatientRowMapper());
    }

    private static class PatientRowMapper implements RowMapper<Patient> {
        @Override
        public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
            Patient patient = new Patient();
            patient.setId(rs.getInt("id"));
            patient.setFirstName(rs.getString("first_name"));
            patient.setLastName(rs.getString("last_name"));
            patient.setDateOfBirth(rs.getString("date_of_birth"));
            patient.setGender(rs.getString("gender"));
            patient.setContactInfo(rs.getString("contact_info"));
            return patient;
        }
    }

    public Patient findById(int id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new PatientRowMapper());
        } catch (Exception e) {
            // Handle the case where no patient is found
            return null;
        }
    }

    public void update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, contact_info = ? WHERE id = ?";
        jdbcTemplate.update(sql, patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(), patient.getGender(), patient.getContactInfo(), patient.getId());
    }
}