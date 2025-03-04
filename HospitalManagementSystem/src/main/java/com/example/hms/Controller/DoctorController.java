package com.example.hms.Controller;

import com.example.hms.Model.Appointment.Appointment;
import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.config.DoctorSessionService;
import com.example.hms.DAORepo.DoctorRepo;
import com.example.hms.DAORepo.LoginRequest;
import com.example.hms.DAORepo.AppointmentRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

   /* @PostMapping("/doctor/loginSuccess")
    public String handleDoctorLogin(String firstName, String lastName, HttpSession session) {
        Doctor doctor = doctorRepo.findByFirstNameAndLastName(firstName, lastName);

        if (doctor != null) {
            session.setAttribute("loggedInDoctor", doctor);
            System.out.println("Login successful for Doctor: " + doctor.getFirstName());
            return "redirect:/Doctor/doctor/dashboard"; // Redirects to the working page
        }

        System.out.println("Login failed. Redirecting back to login page.");
        return "redirect:/Doctor/doctor/login?error=true";
    }*/
    
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
    
/*
    @PostMapping("/updateShift")
    public String updateShiftTimings(String shiftStart, String shiftEnd, HttpSession session) {
        Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");

        if (doctor != null) {
            doctorRepo.updateShift(doctor.getId(), shiftStart, shiftEnd);
            doctor.setShiftStart(shiftStart);
            doctor.setShiftEnd(shiftEnd);
            session.setAttribute("loggedInDoctor", doctor);
        }

        return "redirect:/Doctor/doctor/dashboard"; // Redirect to dashboard after update
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/Doctor/doctor/login";
        // Redirect to login page
    }



    @GetMapping("/dashboard")
    public String doctorDashboard(HttpSession session, Model model) {
        // Retrieve the doctor from session
        Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");

        if (doctor == null) {
            return "redirect:/Doctor/doctor/login"; // Redirect to login if no session
        }

        // Pass doctor data to the model
        model.addAttribute("doctor", doctor);
        return "DoctorDashboard"; // Render dashboard with doctor details
    }

    @GetMapping("/viewAppointments")
    public String viewAppointments(@RequestParam(required = false) String filter, HttpSession session, Model model) {
        Doctor doctor = (Doctor) session.getAttribute("loggedInDoctor");

        List<Appointment> appointments;

        if ("past".equalsIgnoreCase(filter)) {
            appointments = appointmentRepo.findPastAppointments(doctor.getId());
        } else if ("rejected".equalsIgnoreCase(filter)) {
            appointments = appointmentRepo.findRejectedAppointments(doctor.getId());
        } else if ("completed".equalsIgnoreCase(filter)) {
            appointments = appointmentRepo.findCompletedAppointments(doctor.getId());
        } else {
            appointments = appointmentRepo.findCurrentAppointments(doctor.getId());
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("filter", filter);
        return "DoctorViewAppointment";
    }


    @PostMapping("/acceptAppointment")
    public String acceptAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Accepted"
        appointmentRepo.updateStatus(appointmentId, "Accepted");
        return "redirect:/Doctor/doctor/viewAppointments";
    }

    @PostMapping("/rejectAppointment")
    public String rejectAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Rejected"
        appointmentRepo.updateStatus(appointmentId, "Rejected");
        return "redirect:/Doctor/doctor/viewAppointments";
    }

    @PostMapping("/completeAppointment")
    public String completeAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Completed"
        appointmentRepo.updateStatus(appointmentId, "Completed");
        return "redirect:/Doctor/doctor/viewAppointments";
    }*/
    
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
    
}
