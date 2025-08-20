package com.example.studentms.service;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.model.Admin;
import com.example.studentms.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    public boolean validateAdmin(AdminLoginDTO loginDTO) {
        return adminRepository.findByEmail(loginDTO.getEmail())
                .map(admin -> admin.getPassword().equals(loginDTO.getPassword()))
                .orElse(false);
    }
    
    public Admin createAdmin(String email, String password) {
        if (adminRepository.existsByEmail(email)) {
            throw new RuntimeException("Admin with this email already exists");
        }
        
        Admin admin = new Admin(email, password);
        return adminRepository.save(admin);
    }
}