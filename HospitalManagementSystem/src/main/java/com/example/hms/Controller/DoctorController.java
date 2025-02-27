package com.example.hms.Controller;

import com.example.hms.Model.Appointment.Appointment;
import com.example.hms.Model.Doctor.Doctor;
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

import java.util.List;

@Controller
@RequestMapping("/Doctor")
public class DoctorController {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @GetMapping("/doctor/login")
    public String doctorLogin() {
        return "DoctorLogin"; // Render the login page
    }

    @GetMapping("/doctor/register")
    public String doctorRegister() {
        return "DoctorRegistration"; // Render the registration page
    }

    @PostMapping("/doctor/registerSuccess")
    public String handleDoctorRegistration(Doctor doctor) {
        // Save doctor information to the database
        doctorRepo.save(doctor);
        return "redirect:/Doctor/doctor/login"; // Redirect to login after registration
    }

    @PostMapping("/doctor/loginSuccess")
    public String handleDoctorLogin(String firstName, String lastName, HttpSession session) {
        Doctor doctor = doctorRepo.findByFirstNameAndLastName(firstName, lastName);

        if (doctor != null) {
            session.setAttribute("loggedInDoctor", doctor);
            System.out.println("Login successful for Doctor: " + doctor.getFirstName());
            return "redirect:/Doctor/doctor/dashboard"; // Redirects to the working page
        }

        System.out.println("Login failed. Redirecting back to login page.");
        return "redirect:/Doctor/doctor/login?error=true";
    }


    @PostMapping("/doctor/updateShift")
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

    @PostMapping("/doctor/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/Doctor/doctor/login";
        // Redirect to login page
    }



    @GetMapping("/doctor/dashboard")
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

    @GetMapping("/doctor/viewAppointments")
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


    @PostMapping("/doctor/acceptAppointment")
    public String acceptAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Accepted"
        appointmentRepo.updateStatus(appointmentId, "Accepted");
        return "redirect:/Doctor/doctor/viewAppointments";
    }

    @PostMapping("/doctor/rejectAppointment")
    public String rejectAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Rejected"
        appointmentRepo.updateStatus(appointmentId, "Rejected");
        return "redirect:/Doctor/doctor/viewAppointments";
    }

    @PostMapping("/doctor/completeAppointment")
    public String completeAppointment(@RequestParam Long appointmentId) {
        // Update the appointment status to "Completed"
        appointmentRepo.updateStatus(appointmentId, "Completed");
        return "redirect:/Doctor/doctor/viewAppointments";
    }
}
