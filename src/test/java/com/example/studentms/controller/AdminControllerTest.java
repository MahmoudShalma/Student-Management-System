package com.example.studentms.controller;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAdmin_Success() throws Exception {
        // Given
        AdminLoginDTO adminDTO = new AdminLoginDTO("admin@test.com", "password123");
        
        // When & Then
        mockMvc.perform(post("/api/admin/create")
                .contentType(MediaType.APPLICATION_JSON) 
                .content(objectMapper.writeValueAsString(adminDTO))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin created successfully"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));

        verify(adminService).createAdmin("admin@test.com", "password123");
    }

    @Test
    void testCreateAdmin_EmailAlreadyExists() throws Exception {
        // Given
        AdminLoginDTO adminDTO = new AdminLoginDTO("admin@test.com", "password123");
        when(adminService.createAdmin(anyString(), anyString()))
                .thenThrow(new RuntimeException("Admin with this email already exists"));

        // When & Then
        mockMvc.perform(post("/api/admin/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Admin with this email already exists"));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "password123");
        when(adminService.validateAdmin(any(AdminLoginDTO.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "wrongpassword");
        when(adminService.validateAdmin(any(AdminLoginDTO.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void testCheckLogin_LoggedIn() throws Exception {
        // Given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminEmail", "admin@test.com");

        // When & Then
        mockMvc.perform(get("/api/admin/check").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin is logged in"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void testCheckLogin_NotLoggedIn() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/check"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Not logged in"));
    }

    @Test
    void testLogout() throws Exception {
        // Given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminEmail", "admin@test.com");

        // When & Then
        mockMvc.perform(post("/api/admin/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}