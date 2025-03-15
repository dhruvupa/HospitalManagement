package com.example.hms.DAORepo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.hms.Model.Admin.Admin;

@Repository
public class AdminRepo {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

	public Admin findByFirstNameAndLastName(String firstName, String lastName) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND password = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{firstName, lastName}, new AdminRowMapper());
    }
	
	private static class AdminRowMapper implements RowMapper<Admin> {
        @Override
        public Admin mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Admin admin = new Admin();
        	admin.setId(rs.getLong("id"));  // Ensure this line correctly maps id
        	admin.setFirstName(rs.getString("username"));
        	admin.setLastName(rs.getString("password"));
        	admin.setRole(rs.getString("role"));
            return admin;
        }
    }

}
