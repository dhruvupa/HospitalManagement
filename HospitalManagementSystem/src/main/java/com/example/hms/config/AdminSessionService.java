package com.example.hms.config;

import org.springframework.stereotype.Service;

import com.example.hms.Model.Admin.Admin;

@Service
public class AdminSessionService {
	 private Admin loggedInAdmin;

	    public void setLoggedInAdmin(Admin admin) {
	        this.loggedInAdmin = admin;
	    }

	    public Admin getLoggedInAdmin() {
	        return loggedInAdmin;
	    }

	    public void clearLoggedInAdmin() {
	        this.loggedInAdmin = null;
	    }
}


