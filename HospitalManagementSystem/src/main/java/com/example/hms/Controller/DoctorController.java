package com.example.hms.Controller;
import java.util.Optional;

import com.example.hms.DAORepo.*;
import com.example.hms.Model.Appointment.Appointment;
import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.Model.Nurse.Nurse;
import com.example.hms.config.DoctorSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;
    
    @Autowired
    private DoctorSessionService doctorSessionService;

    @Autowired
    private NurseRepo nurseRepo;

    @Autowired
    private DoctorNotesRepo doctorNotesRepo;

    @Autowired
    private NurseTaskRepo nurseTaskRepo;

    @GetMapping("/login")
    public String doctorLogin() {
        return "DoctorLogin"; // Render the login page
    }

    @GetMapping("/register")
    public String doctorRegister() {
        return "DoctorRegistration"; // Render the registration page
    }

    @PostMapping("/registerSuccess")
    public String handleDoctorRegistration(Doctor doctor) {
        // Save doctor information to the database
        doctorRepo.save(doctor);
        return "redirect:/Doctor/doctor/login"; // Redirect to login after registration
    }
    
    @PostMapping("/loginSuccess")
    public ResponseEntity<?> handleDoctorLogin(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Doctor doctor = doctorRepo.findByFirstNameAndLastName(loginRequest.getFirstName(), loginRequest.getLastName());
        if (doctor != null) {
            //session.setAttribute("loggedInDoctor", doctor);
        	doctorSessionService.setLoggedInDoctor(doctor);
            System.out.println("Session in /loginSuccess: " + session.getId());
            return ResponseEntity.ok(Map.of("message", "Login successful", "doctor", doctor));
        }
        return ResponseEntity.status(401).body("{\"message\": \"Invalid credentials\"}");
    }
    
    @PostMapping("/updateShift")
    public ResponseEntity<?> updateShiftTimings(@RequestBody Map<String, String> shiftData, HttpSession session) {
        String shiftStart = shiftData.get("shiftStart");
        String shiftEnd = shiftData.get("shiftEnd");

        Doctor doctor = doctorSessionService.getLoggedInDoctor();

        if (doctor == null) {
        	 return ResponseEntity.status(401).body("{\"message\": \"No Doctor Data Found\"}");
        }

        // Validate shift timings (optional)
        if (!isValidTimeFormat(shiftStart) || !isValidTimeFormat(shiftEnd)) {
        	 return ResponseEntity.status(401).body("{\"message\": \"Invalid Time\"}");
        }

        doctorRepo.updateShift(doctor.getId(), shiftStart, shiftEnd);
        doctor.setShiftStart(shiftStart);
        doctor.setShiftEnd(shiftEnd);
        session.setAttribute("loggedInDoctor", doctor);

        return ResponseEntity.ok(Map.of("message", "Sift updated successfully"));
    }

    // Example validation method
    private boolean isValidTimeFormat(String time) {
        return time.matches("([01]\\d|2[0-3]):[0-5]\\d"); // Validates HH:mm format
    }

    
    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate session
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> doctorDashboard(HttpSession session) {
        //Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");
    	 Doctor doctor = doctorSessionService.getLoggedInDoctor();

        //Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");
        System.out.println("Session in /dashboard: " + session.getId());
        System.out.println("Doctor in session: " + (doctor != null ? doctor.getFirstName() : "null"));

        
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/viewAppointments")
    public ResponseEntity<?> viewAppointments(
        @RequestParam(required = false) String filter, HttpSession session) {

    	 System.out.println("Session in /viewAppointments: " + session.getId());
    	    //Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");
    	 Doctor doctor = doctorSessionService.getLoggedInDoctor();
    	    System.out.println("Doctor in session: " + (doctor != null ? doctor.getFirstName() : "null"));

    	
    	//Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<Appointment> appointments;

        switch (filter != null ? filter.toLowerCase() : "") {
            case "past":
                appointments = appointmentRepo.findPastAppointments(doctor.getId());
                break;
            case "rejected":
                appointments = appointmentRepo.findRejectedAppointments(doctor.getId());
                break;
            case "completed":
                appointments = appointmentRepo.findCompletedAppointments(doctor.getId());
                break;
            default:
                appointments = appointmentRepo.findCurrentAppointments(doctor.getId());
        }

        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/acceptAppointment")
    public ResponseEntity<?> acceptAppointment(@RequestParam Long appointmentId) {
        appointmentRepo.updateStatus(appointmentId, "Accepted");
        return ResponseEntity.ok(Map.of("message", "Appointment accepted successfully"));
    }

    @PostMapping("/rejectAppointment")
    public ResponseEntity<?> rejectAppointment(@RequestParam Long appointmentId) {
        appointmentRepo.updateStatus(appointmentId, "Rejected");
        return ResponseEntity.ok(Map.of("message", "Appointment rejected successfully"));
    }

    @PostMapping("/completeAppointment")
    public ResponseEntity<?> completeAppointment(@RequestParam Long appointmentId) {
        appointmentRepo.updateStatus(appointmentId, "Completed");
        return ResponseEntity.ok(Map.of("message", "Appointment marked as completed"));
    }

    @PostMapping("/assignNurseAndComment")
    public ResponseEntity<?> assignNurseAndComment(@RequestBody Map<String, Object> requestData) {
        System.out.println("Received Request Data: " + requestData); // Debugging log

        // Check if all required fields are present
        if (!requestData.containsKey("patientId") || !requestData.containsKey("nurseId") || !requestData.containsKey("comment")) {
            System.out.println("Missing required fields in request!");
            return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
        }

        try {
        	Doctor doctor = doctorSessionService.getLoggedInDoctor();
        	Long doctorId = doctor.getId();
            int patientId = Integer.parseInt(requestData.get("patientId").toString()); // Ensure int usage
            int nurseId = Integer.parseInt(requestData.get("nurseId").toString()); // Ensure int usage
            String comment = requestData.get("comment").toString();

            System.out.println("Extracted Data: patientId=" + patientId + ", nurseId=" + nurseId + ", comment=" + comment);

            Optional<Nurse> nurseOpt = nurseRepo.findById(nurseId); // Now correctly using int

            // Check if patient exists in the database
            boolean patientExists = !appointmentRepo.findByPatientId(patientId).isEmpty();

            if (patientExists && nurseOpt.isPresent()) {
                Nurse nurse = nurseOpt.get();

                // Store the doctor's comment in nurse_task table
                nurseTaskRepo.createTask( doctorId,nurseId, patientId, comment); // Using int values

                return ResponseEntity.ok(Map.of("message", "Nurse assigned and comment saved successfully!"));
            } else {
                System.out.println("Invalid patient or nurse ID!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid patient or nurse ID"));
            }
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error processing request"));
        }
    }

}
