package com.example.hms.config;

import com.example.hms.Model.Patient.Patient;
import org.springframework.stereotype.Service;

@Service
public class PatientSessionService {
    private Patient loggedInPatient;

    public void setLoggedInPatient(Patient patient) {
        this.loggedInPatient = patient;
    }

    public Patient getLoggedInPatient() {
        return loggedInPatient;
    }

    public void clearLoggedInPatient() {
        this.loggedInPatient = null;
    }
}