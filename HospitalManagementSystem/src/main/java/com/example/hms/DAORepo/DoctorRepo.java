package com.example.hms.DAORepo;

import com.example.hms.Model.Doctor.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DoctorRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Doctor doctor) {
        String sql = "INSERT INTO doctors (first_name, last_name, specialization, contact_info, shift_start, shift_end) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, doctor.getFirstName(), doctor.getLastName(), doctor.getSpecialization(), doctor.getContactInfo(), doctor.getShiftStart(), doctor.getShiftEnd());
    }

    public Doctor findByFirstNameAndLastName(String firstName, String lastName) {
        String sql = "SELECT * FROM doctors WHERE first_name = ? AND last_name = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{firstName, lastName}, new DoctorRowMapper());
    }

    public Doctor findById(int doctorId) {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{doctorId}, new DoctorRowMapper());
        } catch (Exception e) {
            // Handle the case where no doctor is found
            return null;
        }
    }
    // to reset availibility time
    public String getShiftEnd(int doctorId) {
        String sql = "SELECT shift_end FROM doctors WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{doctorId}, String.class);
    }

    public void updateShift(Long doctorId, String shiftStart, String shiftEnd) { // Changed from int to Long
        String sql = "UPDATE doctors SET shift_start = ?, shift_end = ? WHERE id = ?";
        jdbcTemplate.update(sql, shiftStart, shiftEnd, doctorId);
    }


    public List<Doctor> findAll() {
        String sql = "SELECT * FROM doctors";
        return jdbcTemplate.query(sql, new DoctorRowMapper());
    }

    public List<String> findBookedSlotsByDoctorId(Long doctorId) {
        String sql = "SELECT time_slot FROM appointments WHERE doctor_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, doctorId);
    }

    private static class DoctorRowMapper implements RowMapper<Doctor> {
        @Override
        public Doctor mapRow(ResultSet rs, int rowNum) throws SQLException {
            Doctor doctor = new Doctor();
            doctor.setId(rs.getLong("id"));  // Ensure this line correctly maps id
            doctor.setFirstName(rs.getString("first_name"));
            doctor.setLastName(rs.getString("last_name"));
            doctor.setSpecialization(rs.getString("specialization"));
            doctor.setContactInfo(rs.getString("contact_info"));
            doctor.setShiftStart(rs.getString("shift_start"));
            doctor.setShiftEnd(rs.getString("shift_end"));
            return doctor;
        }
    }
}