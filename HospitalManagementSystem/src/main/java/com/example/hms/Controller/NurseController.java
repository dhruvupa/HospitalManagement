package com.example.hms.Controller;

import com.example.hms.DAORepo.LoginRequest;
import com.example.hms.DAORepo.NurseRepo;
import com.example.hms.Model.Nurse.Nurse;
import com.example.hms.Model.Nurse.NurseTask;
import com.example.hms.config.NurseSessionService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nurse")
public class NurseController {
    @Autowired
    private NurseRepo nurseRepo;
    
    @Autowired
    private NurseSessionService nurseSessionService;

    // ✅ Correct method to fetch all nurses
    @GetMapping("/nurses")
    public ResponseEntity<List<Nurse>> getAllNurses() {
        List<Nurse> nurses = nurseRepo.findAll();
        return ResponseEntity.ok(nurses);
    }
    
    @PostMapping("/loginSuccess")
    public ResponseEntity<?> handleNurseLogin(@RequestBody LoginRequest loginRequest, HttpSession session) {
    	Nurse nurse = nurseRepo.findByFirstNameAndLastName(loginRequest.getFirstName(), loginRequest.getLastName());
        if (nurse != null) {
        	nurseSessionService.setLoggedInNurse(nurse); // Use PatientSessionService
            System.out.println("Session in /loginSuccess: " + session.getId());
            return ResponseEntity.ok(Map.of("message", "Login successful", "nurse", nurse));
        }
        return ResponseEntity.status(401).body("{\"message\": \"Invalid credentials\"}");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    
    // Dashboard Endpoint
    @GetMapping("/dashboard")
    public ResponseEntity<?> patientDashboard(HttpSession session) {
    	Nurse nurse = nurseSessionService.getLoggedInNurse(); 
        if (nurse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<NurseTask> nurseTask = nurseRepo.findDoctorNotesByNurseId(nurse.getId());
        return ResponseEntity.ok(Map.of("nurse", nurse, "nurseTask", nurseTask));
    }


}
