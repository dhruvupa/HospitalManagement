package com.example.hms.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/Admin")
public class AdminController {

    // Patient Login Page
    @GetMapping("/patient/login")
    public String patientLogin() {
        return "patientLogin";
    }

    // Doctor Login Page
    @GetMapping("/doctor/login")
    public String doctorLogin() {
        return "doctor-login";
    }

    // Patient Registration Page
    @GetMapping("/patient/register")
    public String patientRegister() {
        return "patient-register";
    }

    // Doctor Registration Page
    @GetMapping("/doctor/register")
    public String doctorRegister() {
        return "doctor-register";
    }

    // Handle Patient Login Form Submission
    @PostMapping("/patient/login")
    public String handlePatientLogin(String username, String password) {
        // Add logic to authenticate patient
        return "redirect:/patient/dashboard"; // Redirect to patient dashboard
    }

    // Handle Doctor Login Form Submission
    @PostMapping("/doctor/login")
    public String handleDoctorLogin(String username, String password) {
        // Add logic to authenticate doctor
        return "redirect:/doctor/dashboard"; // Redirect to doctor dashboard
    }

    // Handle Patient Registration Form Submission
    @PostMapping("/patient/register")
    public String handlePatientRegistration(String username, String email, String password, String dob) {
        // Add logic to register patient
        return "redirect:/auth/patient/login"; // Redirect to patient login page
    }

    // Handle Doctor Registration Form Submission
    @PostMapping("/doctor/register")
    public String handleDoctorRegistration(String username, String email, String password, String specialization) {
        // Add logic to register doctor
        return "redirect:/auth/doctor/login"; // Redirect to doctor login page
    }
}