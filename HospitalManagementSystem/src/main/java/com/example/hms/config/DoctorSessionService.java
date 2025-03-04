package com.example.hms.config;
import org.springframework.stereotype.Service;

import com.example.hms.Model.Doctor.Doctor;

@Service
public class DoctorSessionService {
    private Doctor loggedInDoctor;

    public Doctor getLoggedInDoctor() {
        return loggedInDoctor;
    }

    public void setLoggedInDoctor(Doctor doctor) {
        this.loggedInDoctor = doctor;
    }

    public void clearDoctor() {
        this.loggedInDoctor = null;
    }
}
