package com.example.hms.DAORepo;

import com.example.hms.Model.Nurse.Nurse;
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
}
