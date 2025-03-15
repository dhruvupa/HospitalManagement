package com.example.hms.Controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hms.DAORepo.AdminRepo;
import com.example.hms.DAORepo.DoctorRepo;
import com.example.hms.DAORepo.LoginRequest;
import com.example.hms.DAORepo.NurseRepo;
import com.example.hms.Model.Admin.Admin;
import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.Model.Nurse.Nurse;
import com.example.hms.config.AdminSessionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private NurseRepo nurseRepo;

    @Autowired
    private AdminSessionService adminSessionService;

    // Admin Login Endpoint
    @PostMapping("/loginSuccess")
    public ResponseEntity<?> handleAdminLogin(@RequestBody LoginRequest loginRequest) {

        Admin admin = adminRepo.findByFirstNameAndLastName(loginRequest.getFirstName(), loginRequest.getLastName());

        if (admin != null) {
            adminSessionService.setLoggedInAdmin(admin);
            return ResponseEntity.ok(Map.of("message", "Login successful", "admin", admin));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
    }

    // Admin Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Add a Doctor
    @PostMapping("/addDoctor")
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor, HttpSession session) {
        Admin admin = adminSessionService.getLoggedInAdmin();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        doctorRepo.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor added successfully", "doctor", doctor));
    }

    // Add a Nurse
    @PostMapping("/addNurse")
    public ResponseEntity<?> addNurse(@RequestBody Nurse nurse, HttpSession session) {
        Admin admin = adminSessionService.getLoggedInAdmin();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        nurseRepo.save(nurse);
        return ResponseEntity.ok(Map.of("message", "Nurse added successfully", "nurse", nurse));
    }

    // Get List of All Doctors
    @GetMapping("/listDoctors")
    public ResponseEntity<?> listDoctors(HttpSession session) {
        Admin admin = adminSessionService.getLoggedInAdmin();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<Doctor> doctors = doctorRepo.findAll();
        return ResponseEntity.ok(doctors);
    }

    // Get List of All Nurses
    @GetMapping("/listNurses")
    public ResponseEntity<?> listNurses(HttpSession session) {
        Admin admin = adminSessionService.getLoggedInAdmin();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<Nurse> nurses = nurseRepo.findAll();
        return ResponseEntity.ok(nurses);
    }
}
