package com.example.hms.Controller;

import com.example.hms.Model.Patient.Patient;
import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.Model.Appointment.Appointment;
import com.example.hms.DAORepo.PatientRepo;
import com.example.hms.DAORepo.DoctorRepo;
import com.example.hms.DAORepo.LoginRequest;
import com.example.hms.DAORepo.AppointmentRepo;
import com.example.hms.config.PatientSessionService; // Add this import
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patient")
public class PatientController {
    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private PatientSessionService patientSessionService; // Add this

    // Login and Registration Endpoints
    @GetMapping("/login")
    public String patientLogin() {
        return "PatientLogin";
    }

    @GetMapping("/register")
    public String patientRegister() {
        return "PatientRegistration";
    }

    @PostMapping("/loginSuccess")
    public ResponseEntity<?> handlePatientLogin(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Patient patient = patientRepo.findByFirstNameAndLastName(loginRequest.getFirstName(), loginRequest.getLastName());
        if (patient != null) {
            patientSessionService.setLoggedInPatient(patient); // Use PatientSessionService
            System.out.println("Session in /loginSuccess: " + session.getId());
            return ResponseEntity.ok(Map.of("message", "Login successful", "patient", patient));
        }
        return ResponseEntity.status(401).body("{\"message\": \"Invalid credentials\"}");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/registerSuccess")
    public ResponseEntity<String> handlePatientRegistration(@RequestBody Patient patient) {
        try {
            patientRepo.save(patient);
            return ResponseEntity.ok("Registration Successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration Failed: " + e.getMessage());
        }
    }

    // Dashboard Endpoint
    @GetMapping("/dashboard")
    public ResponseEntity<?> patientDashboard(HttpSession session) {
        Patient patient = patientSessionService.getLoggedInPatient(); // Use PatientSessionService
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<Doctor> doctors = doctorRepo.findAll();
        return ResponseEntity.ok(Map.of("patient", patient, "doctors", doctors));
    }

    // Update Patient Details Endpoints
    @GetMapping("/updateDetails")
    public ResponseEntity<?> updatePatientDetailsPage() {
        int patientId = 1; // Example patient ID
        Patient patient = patientRepo.findById(patientId);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found"));
        }
        return ResponseEntity.ok(patient);
    }

    @PostMapping("/updateDetails")
    public ResponseEntity<?> updatePatientDetails(@RequestBody Patient patient) {
        try {
            patientRepo.update(patient);
            return ResponseEntity.ok(Map.of("message", "Patient details updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to update patient details"));
        }
    }

    // View Appointments Endpoint
    @GetMapping("/viewAppointments")
    public ResponseEntity<?> viewAppointmentsPage(
            @RequestParam(required = false) String filter,
            HttpSession session) {

        Patient patient = patientSessionService.getLoggedInPatient(); // Use PatientSessionService
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        List<Appointment> appointments = appointmentRepo.findByPatientId(patient.getId());

        if (filter != null && filter.equals("past")) {
            appointments = appointments.stream()
                    .filter(appointment -> !appointment.getStatus().equals("scheduled")
                            && !appointment.getStatus().equals("Accepted"))
                    .collect(Collectors.toList());
        } else {
            appointments = appointments.stream()
                    .filter(appointment -> appointment.getStatus().equals("scheduled")
                            || appointment.getStatus().equals("Accepted"))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(Map.of("appointments", appointments, "filter", filter));
    }

    // Fetch Booked Slots Endpoint
    @GetMapping("/getBookedSlots")
    @ResponseBody
    public ResponseEntity<?> getBookedSlots(@RequestParam int doctorId) {
        List<String> bookedSlots = appointmentRepo.findBookedSlotsByDoctorId(doctorId);
        return ResponseEntity.ok(bookedSlots);
    }

    // Fetch Available Time Slots Endpoint
    @GetMapping("/getTimeSlots")
    @ResponseBody
    public ResponseEntity<?> getTimeSlots(@RequestParam int doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
        }

        List<String> bookedSlots = appointmentRepo.findBookedSlotsByDoctorId(doctorId);
        List<String> timeSlots = generateTimeSlots(doctor.getShiftStart(), doctor.getShiftEnd(), bookedSlots);
        return ResponseEntity.ok(timeSlots);
    }

    private List<String> generateTimeSlots(String shiftStart, String shiftEnd, List<String> bookedSlots) {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.parse(shiftStart);
        LocalTime end = LocalTime.parse(shiftEnd);

        while (start.isBefore(end)) {
            String slot = start.toString() + " - " + start.plusMinutes(60).toString();
            if (!bookedSlots.contains(slot)) {
                slots.add(slot);
            }
            start = start.plusMinutes(60);
        }

        return slots;
    }

    // Book Appointment Endpoint
    @PostMapping("/bookAppointment")
    public ResponseEntity<?> bookAppointment(@RequestParam int doctorId, @RequestParam String timeSlot, HttpSession session) {
        Patient patient = patientSessionService.getLoggedInPatient();
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        System.out.println("Booking Appointment - Doctor ID: " + doctorId); // Debugging
        System.out.println("Booking Appointment - Time Slot: " + timeSlot); // Debugging

        try {
            LocalDateTime appointmentDate = parseTimeSlot(timeSlot);
            System.out.println("Parsed Appointment Date: " + appointmentDate); // Debugging

            Appointment appointment = new Appointment();
            appointment.setPatientId((long) patient.getId());
            appointment.setDoctorId((long) doctorId);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStatus("scheduled");

            appointmentRepo.save(appointment);
            System.out.println("Appointment Saved Successfully"); // Debugging

            return ResponseEntity.ok(Map.of("message", "Appointment booked successfully"));
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage()); // Debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Booking failed: " + e.getMessage()));
        }
    }

    private LocalDateTime parseTimeSlot(String timeSlot) {
        try {
            String startTime = timeSlot.split(" - ")[0]; // Extract the start time
            LocalTime time = LocalTime.parse(startTime); // Parse the start time
            return LocalDateTime.now().with(time); // Combine with the current date
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time slot format: " + timeSlot);
        }
    }
}