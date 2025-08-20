package com.example.studentms.controller;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginDTO loginDTO, HttpSession session) { 
        logger.info("Admin login attempt for email: {}", loginDTO.getEmail()); 
        
        if (adminService.validateAdmin(loginDTO)) {
            // Store admin email in session
            session.setAttribute("adminEmail", loginDTO.getEmail());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("email", loginDTO.getEmail());
            
            logger.info("Admin login successful for email: {}", loginDTO.getEmail());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Admin login failed for email: {}", loginDTO.getEmail());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String adminEmail = (String) session.getAttribute("adminEmail");
        session.invalidate();
        
        logger.info("Admin logout for email: {}", adminEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(HttpSession session) {
        String adminEmail = (String) session.getAttribute("adminEmail");
        
        if (adminEmail != null) {
            logger.debug("Admin session check - logged in: {}", adminEmail);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin is logged in");
            response.put("email", adminEmail);
            return ResponseEntity.ok(response);
        } else {
            logger.debug("Admin session check - not logged in");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not logged in");
            return ResponseEntity.status(401).body(error);
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminLoginDTO adminDTO) {
        logger.info("Creating new admin with email: {}", adminDTO.getEmail());
        
        try {
            adminService.createAdmin(adminDTO.getEmail(), adminDTO.getPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin created successfully");
            response.put("email", adminDTO.getEmail());
            
            logger.info("Admin created successfully with email: {}", adminDTO.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to create admin with email: {} - Error: {}", adminDTO.getEmail(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}