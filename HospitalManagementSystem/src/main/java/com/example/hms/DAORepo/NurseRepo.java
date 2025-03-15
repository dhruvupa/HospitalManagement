package com.example.hms.DAORepo;

import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.Model.Nurse.Nurse;
import com.example.hms.Model.Nurse.NurseTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class NurseRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<Nurse> findById(int nurseId) {  // Use int instead of Long
        String sql = "SELECT * FROM nurses WHERE id = ?";
        try {
            Nurse nurse = jdbcTemplate.queryForObject(sql, new Object[]{nurseId}, new NurseRowMapper());
            return Optional.ofNullable(nurse);
        } catch (Exception e) {
            return Optional.empty(); // Return an empty Optional if no nurse is found
        }
    }

    public List<Nurse> findAll() {
        String sql = "SELECT id, first_name, last_name, contact_info FROM nurses";
        return jdbcTemplate.query(sql, new NurseRowMapper());
    }

    private static class NurseRowMapper implements RowMapper<Nurse> {
        @Override
        public Nurse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Nurse nurse = new Nurse();
            nurse.setId(rs.getInt("id"));  // Ensure ID is stored as int
            nurse.setFirstName(rs.getString("first_name"));
            nurse.setLastName(rs.getString("last_name"));
            nurse.setContactInfo(rs.getString("contact_info"));
            return nurse;
        }
    }
    
    private static class NurseTaskRowMapper2 implements RowMapper<NurseTask> {
        @Override
        public NurseTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            NurseTask task = new NurseTask();
            task.setId(rs.getInt("id"));
            task.setNurseId(rs.getInt("nurse_id"));
            task.setPatientId(rs.getInt("patient_id"));
            task.setDoctorFirstName(rs.getString("doctor_first_name"));
            task.setDoctorLastName(rs.getString("doctor_last_name"));
            task.setPatientFirstName(rs.getString("patient_first_name"));
            task.setPatientLastName(rs.getString("patient_last_name"));
            task.setNote(rs.getString("note"));
            task.setCreatedAt(rs.getTimestamp("created_at"));
            return task;
        }
    }

    public Nurse findByFirstNameAndLastName(String firstName, String lastName) {
        String sql = "SELECT * FROM nurses WHERE first_name = ? AND last_name = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{firstName, lastName}, new NurseRowMapper());
    }
    
    public List<NurseTask> findDoctorNotesByNurseId(int nurseId) {
    	String sql = "SELECT dn.id, dn.nurse_id, dn.patient_id, d.first_name AS doctor_first_name, " +
                "d.last_name AS doctor_last_name, p.first_name AS patient_first_name, " +
                "p.last_name AS patient_last_name, dn.note, dn.created_at " +
                "FROM doctor_notes dn " +
                "JOIN doctors d ON dn.doctor_id = d.id " +
                "JOIN patients p ON dn.patient_id = p.id " +
                "WHERE dn.nurse_id = ?";


        return jdbcTemplate.query(sql, new Object[]{nurseId}, new NurseTaskRowMapper2());
    }
    
    public void save(Nurse nurse) {
        String sql = "INSERT INTO nurses (first_name, last_name, contact_info) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, nurse.getFirstName(), nurse.getLastName(), nurse.getContactInfo());
    }

    
}
