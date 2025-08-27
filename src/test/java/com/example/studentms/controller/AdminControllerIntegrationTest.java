package com.example.studentms.controller;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.model.Admin;
import com.example.studentms.repository.AdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AdminController
 * Tests full HTTP request/response cycle including session management
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never"
})
@Transactional
class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student-management/api/admin";
        adminRepository.deleteAll();
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Given - Create admin
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);

        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "wrongpassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> request = new HttpEntity<>(loginDTO, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/login", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid email or password");
    }

    @Test
    void testLogin_NonExistentAdmin() {
        // Given
        AdminLoginDTO loginDTO = new AdminLoginDTO("nonexistent@test.com", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> request = new HttpEntity<>(loginDTO, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/login", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid email or password");
    }

    @Test
    void testCompleteLoginFlow() {
        // Step 1: Create admin
        AdminLoginDTO createDTO = new AdminLoginDTO("admin@test.com", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> createRequest = new HttpEntity<>(createDTO, headers);

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            baseUrl + "/create", createRequest, String.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 2: Login
        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "password123");
        HttpEntity<AdminLoginDTO> loginRequest = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            baseUrl + "/login", loginRequest, String.class);
        
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains("Login successful");
    }

    @Test
    void testCreateAdmin_InvalidData() {
        // Given - Invalid email format
        AdminLoginDTO adminDTO = new AdminLoginDTO("invalid-email", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> request = new HttpEntity<>(adminDTO, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/create", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateAdmin_EmptyPassword() {
        // Given - Empty password
        AdminLoginDTO adminDTO = new AdminLoginDTO("admin@test.com", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> request = new HttpEntity<>(adminDTO, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/create", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testDatabaseOperations() {
        // Test direct database operations to verify the admin system works
        
        // Create admin directly
        Admin admin = new Admin("admin@test.com", "password123");
        Admin savedAdmin = adminRepository.save(admin);
        
        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@test.com");
        
        // Test findByEmail
        Admin foundAdmin = adminRepository.findByEmail("admin@test.com").orElse(null);
        assertThat(foundAdmin).isNotNull();
        assertThat(foundAdmin.getPassword()).isEqualTo("password123");
        
        // Test existsByEmail
        assertThat(adminRepository.existsByEmail("admin@test.com")).isTrue();
        assertThat(adminRepository.existsByEmail("nonexistent@test.com")).isFalse();
        
        // Test count
        assertThat(adminRepository.count()).isEqualTo(1);
        
        // Test delete
        adminRepository.deleteById(savedAdmin.getId());
        assertThat(adminRepository.count()).isEqualTo(0);
    }
}