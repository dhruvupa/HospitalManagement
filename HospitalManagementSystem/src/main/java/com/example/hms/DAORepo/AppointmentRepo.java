package com.example.hms.DAORepo;

import com.example.hms.Model.Appointment.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<Appointment> findByPatientId(int patientId) {
        String sql = "SELECT a.id, d.first_name AS doctor_name, p.first_name AS patient_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "JOIN patients p ON a.patient_id = p.id " + // Add this line
                "WHERE a.patient_id = ?";

        return jdbcTemplate.query(sql, new Object[]{patientId}, new AppointmentRowMapper());
    }


    public List<String> findAvailableTimeSlots(int doctorId) {
        String sql = "SELECT CONCAT(DATE_FORMAT(a.appointment_date, '%H:%i'), ' - ', DATE_FORMAT(DATE_ADD(a.appointment_date, INTERVAL 30 MINUTE), '%H:%i')) AS time_slot " +
                "FROM appointments a " +
                "WHERE a.doctor_id = ? AND a.status = 'Scheduled'";
        return jdbcTemplate.queryForList(sql, new Object[]{doctorId}, String.class);
    }

    public void save(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, appointment.getPatientId(), appointment.getDoctorId(), appointment.getAppointmentDate(), appointment.getStatus());
    }

    public List<String> findBookedSlotsByDoctorId(int doctorId) {
        String sql = "SELECT CONCAT(DATE_FORMAT(appointment_date, '%H:%i'), ' - ', DATE_FORMAT(DATE_ADD(appointment_date, INTERVAL 60 MINUTE), '%H:%i')) AS time_slot " +
                "FROM appointments " +
                "WHERE doctor_id = ? AND status = 'Scheduled'";
        return jdbcTemplate.queryForList(sql, new Object[]{doctorId}, String.class);
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        String sql = "SELECT a.id, p.first_name AS patient_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "WHERE a.doctor_id = ? AND a.status NOT IN ('Completed', 'Rejected')";

        return jdbcTemplate.query(sql, new Object[]{doctorId}, new AppointmentRowMapper());
    }

    public void updateStatus(Long appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, appointmentId);
    }

    public List<Appointment> findCurrentAppointments(Long doctorId) {
        String sql = "SELECT a.id, p.first_name AS patient_name, d.first_name AS doctor_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.doctor_id = ? AND a.status IN ('Scheduled', 'Accepted')";

        return jdbcTemplate.query(sql, new Object[]{doctorId}, new AppointmentRowMapper());
    }

    public List<Appointment> findPastAppointments(Long doctorId) {
        String sql = "SELECT a.id, p.first_name AS patient_name, d.first_name AS doctor_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " + // Add this line
                "WHERE a.doctor_id = ? AND a.status IN ('Completed', 'Rejected')";

        return jdbcTemplate.query(sql, new Object[]{doctorId}, new AppointmentRowMapper());
    }

    public List<Appointment> findRejectedAppointments(Long doctorId) {
        String sql = "SELECT a.id, p.first_name AS patient_name, d.first_name AS doctor_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " + // Add this line
                "WHERE a.doctor_id = ? AND a.status = 'Rejected'";

        return jdbcTemplate.query(sql, new Object[]{doctorId}, new AppointmentRowMapper());
    }

    public List<Appointment> findCompletedAppointments(Long doctorId) {
        String sql = "SELECT a.id, p.first_name AS patient_name, d.first_name AS doctor_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " + // Add this line
                "WHERE a.doctor_id = ? AND a.status = 'Completed'";

        return jdbcTemplate.query(sql, new Object[]{doctorId}, new AppointmentRowMapper());
    }

    // In AppointmentRepo.java
    public void assignNurse(Long appointmentId, Integer nurseId) {
        String sql = "UPDATE appointments SET nurse_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, nurseId, appointmentId);
    }

    public Optional<Appointment> findById(Long appointmentId) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try {
            Appointment appointment = jdbcTemplate.queryForObject(sql, new Object[]{appointmentId}, new AppointmentRowMapper());
            return Optional.ofNullable(appointment);
        } catch (Exception e) {
            return Optional.empty(); // Return an empty Optional if no appointment is found
        }
    }

    private static class AppointmentRowMapper implements RowMapper<Appointment> {
        @Override
        public Appointment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Appointment appointment = new Appointment();
            appointment.setId(rs.getLong("id"));
            appointment.setDoctorName(rs.getString("doctor_name")); // Correct column
            appointment.setPatientName(rs.getString("patient_name")); // Now available
            appointment.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
            appointment.setStatus(rs.getString("status"));
            return appointment;
        }
    }
}