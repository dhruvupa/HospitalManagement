package com.example.hms.Controller;

import com.example.hms.Model.Patient.Patient;
import com.example.hms.Model.Doctor.Doctor;
import com.example.hms.Model.Appointment.Appointment;
import com.example.hms.DAORepo.PatientRepo;
import com.example.hms.DAORepo.DoctorRepo;
import com.example.hms.DAORepo.AppointmentRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/Patient")
public class PatientController {
    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @GetMapping("/patient/login")
    public String patientLogin() {
        return "PatientLogin";
    }

    @GetMapping("/patient/register")
    public String patientRegister() {
        return "PatientRegistration";
    }

    @PostMapping("/patient/loginSuccess")
    public String handlePatientLogin(String firstName, String lastName, HttpSession session) {
        Patient patient = patientRepo.findByFirstNameAndLastName(firstName, lastName);
        if (patient != null) {
            session.setAttribute("loggedInPatient", patient);
            return "redirect:/Patient/patient/dashboard";
        }
        return "redirect:/Patient/patient/login";
    }

    @PostMapping("/patient/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/Patient/patient/login";
        // Redirect to login page
    }

    @PostMapping("/patient/registerSuccess")
    public String handlePatientRegistration(Patient patient) {
        patientRepo.save(patient);
        return "redirect:/Patient/patient/login";
    }



    @GetMapping("/patient/dashboard")
    public String patientDashboard(HttpSession session, Model model) {
        // Retrieve the doctor from session
        Patient patient = (Patient) session.getAttribute("loggedInPatient");

        if (patient == null) {
            return "redirect:/Patient/patient/login"; // Redirect to login if no session
        }


        List<Doctor> doctors = doctorRepo.findAll();
        model.addAttribute("doctors", doctors);

        // Pass doctor data to the model
        model.addAttribute("patient", patient);
        return "PatientDashboard"; // Render dashboard with doctor details
    }

    @GetMapping("/patient/updateDetails")
    public String updatePatientDetailsPage(Model model) {
        int patientId = 1; // Example patient ID
        Patient patient = patientRepo.findById(patientId);
        model.addAttribute("patient", patient);
        return "UpdatePatientDetails";
    }

    @PostMapping("/patient/updateDetails")
    public String updatePatientDetails(Patient patient) {
        patientRepo.update(patient);
        return "redirect:/Patient/patient/dashboard";
    }

    @GetMapping("/patient/viewAppointments")
    public String viewAppointmentsPage(
            @RequestParam(required = false) String filter,
            HttpSession session,
            Model model) {

        // Retrieve the patient from the session
        Patient patient = (Patient) session.getAttribute("loggedInPatient");

        if (patient == null) {
            return "redirect:/Patient/patient/login"; // Redirect to login if no session
        }

        // Fetch appointments for the logged-in patient
        List<Appointment> appointments = appointmentRepo.findByPatientId(patient.getId());

        // Filter appointments based on the filter parameter
        if (filter != null && filter.equals("past")) {
            // Show only past bookings (statuses other than "scheduled" and "Accepted")
            appointments = appointments.stream()
                    .filter(appointment -> !appointment.getStatus().equals("scheduled")
                            && !appointment.getStatus().equals("Accepted"))
                    .collect(Collectors.toList());
        } else {
            // Show only "scheduled" and "Accepted" appointments by default
            appointments = appointments.stream()
                    .filter(appointment -> appointment.getStatus().equals("scheduled")
                            || appointment.getStatus().equals("Accepted"))
                    .collect(Collectors.toList());
        }

        // Add appointments and filter to the model
        model.addAttribute("appointments", appointments);
        model.addAttribute("filter", filter);

        return "PatientViewAppointment";
    }

    @GetMapping("/patient/getBookedSlots")
    @ResponseBody
    public List<String> getBookedSlots(@RequestParam int doctorId) {
        return appointmentRepo.findBookedSlotsByDoctorId(doctorId);
    }

    @GetMapping("/patient/getTimeSlots")
    @ResponseBody
    public List<String> getTimeSlots(@RequestParam int doctorId) {
        // Fetch doctor's shift timings
        Doctor doctor = doctorRepo.findById(doctorId);
        if (doctor == null) {
            return new ArrayList<>(); // Return empty list if doctor not found
        }

        // Fetch booked slots for the doctor
        List<String> bookedSlots = appointmentRepo.findBookedSlotsByDoctorId(doctorId);

        // Generate all slots and exclude booked ones
        return generateTimeSlots(doctor.getShiftStart(), doctor.getShiftEnd(), bookedSlots);
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

    @GetMapping("/patient/getShiftEnd")
    @ResponseBody
    public String getShiftEnd(@RequestParam int doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId);
        return (doctor != null) ? doctor.getShiftEnd() : "";
    }


    @PostMapping("/patient/bookAppointment")
    public String bookAppointment(@RequestParam int doctorId, @RequestParam String timeSlot, HttpSession session) {
        Patient patient = (Patient) session.getAttribute("loggedInPatient");
        if (patient == null) {
            return "redirect:/Patient/patient/login";
        }

        try {
            LocalDateTime appointmentDate = parseTimeSlot(timeSlot);
            Appointment appointment = new Appointment();
            appointment.setPatientId((long) patient.getId());
            appointment.setDoctorId((long) doctorId);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStatus("scheduled"); // Ensure status is set to "Scheduled"
            appointmentRepo.save(appointment);

            return "redirect:/Patient/patient/dashboard?success=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/Patient/patient/dashboard?error=Booking failed";
        }
    }

    private LocalDateTime parseTimeSlot(String timeSlot) {
        // Example timeSlot format: "10:00 - 10:30"
        String startTime = timeSlot.split(" - ")[0]; // Extract the start time
        LocalTime time = LocalTime.parse(startTime); // Parse the start time
        return LocalDateTime.now().with(time); // Combine with the current date
    }
}