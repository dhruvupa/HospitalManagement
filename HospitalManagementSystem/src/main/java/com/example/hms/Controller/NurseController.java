package com.example.hms.Controller;

import com.example.hms.DAORepo.NurseRepo;
import com.example.hms.Model.Nurse.Nurse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/nurses")
public class NurseController {
    @Autowired
    private NurseRepo nurseRepo;

    // ✅ Correct method to fetch all nurses
    @GetMapping("/nurse")
    public ResponseEntity<List<Nurse>> getAllNurses() {
        List<Nurse> nurses = nurseRepo.findAll();
        return ResponseEntity.ok(nurses);
    }
}
