package com.example.hms.DAORepo;

import com.example.hms.Model.Nurse.NurseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NurseTaskRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createTask(int nurseId, int patientId, String note) {
        System.out.println("Inserting Task: Nurse ID=" + nurseId + ", Patient ID=" + patientId + ", Note=" + note); // Debug log
        String sql = "INSERT INTO nurse_task (nurse_id, patient_id, note, created_at) VALUES (?, ?, ?, NOW())";
        jdbcTemplate.update(sql, nurseId, patientId, note);
    }

    public List<NurseTask> getTasksByPatientId(int patientId) {
        String sql = "SELECT * FROM nurse_task WHERE patient_id = ?";
        return jdbcTemplate.query(sql, new Object[]{patientId}, new NurseTaskRowMapper());
    }

    private static class NurseTaskRowMapper implements RowMapper<NurseTask> {
        @Override
        public NurseTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            NurseTask task = new NurseTask();
            task.setId(rs.getInt("id"));
            task.setNurseId(rs.getInt("nurse_id"));
            task.setPatientId(rs.getInt("patient_id"));
            task.setNote(rs.getString("note"));
            task.setCreatedAt(rs.getTimestamp("created_at"));
            return task;
        }
    }
}
