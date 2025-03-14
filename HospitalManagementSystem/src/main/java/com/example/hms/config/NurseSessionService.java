package com.example.hms.config;

import org.springframework.stereotype.Service;

import com.example.hms.Model.Nurse.Nurse;

@Service
public class NurseSessionService {
    private Nurse loggedInNurse;

    public void setLoggedInNurse(Nurse nurse) {
        this.loggedInNurse = nurse;
    }

    public Nurse getLoggedInNurse() {
        return loggedInNurse;
    }

    public void clearLoggedInNurse() {
        this.loggedInNurse = null;
    }
}
