package com.example.studentms.controller;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.model.Admin;
import com.example.studentms.model.Student;
import com.example.studentms.repository.AdminRepository;
import com.example.studentms.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentController
 * Tests full HTTP request/response cycle including admin authentication
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
class StudentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String adminUrl;
    private HttpHeaders authenticatedHeaders;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student-management/api/students";
        adminUrl = "http://localhost:" + port + "/student-management/api/admin";
        
        // Clean up repositories
        studentRepository.deleteAll();
        adminRepository.deleteAll();
        
        // Create and login admin to get authenticated session
        authenticatedHeaders = createAndLoginAdmin();
    }

    private HttpHeaders createAndLoginAdmin() {
        // Generate unique email to avoid conflicts between tests
        String uniqueEmail = "admin" + System.currentTimeMillis() + "@test.com";
        
        // Create admin using the create endpoint instead of direct repository save
        AdminLoginDTO createDTO = new AdminLoginDTO(uniqueEmail, "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> createRequest = new HttpEntity<>(createDTO, headers);

        // First create the admin
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            adminUrl + "/create", createRequest, String.class);

        if (!createResponse.getStatusCode().equals(HttpStatus.OK)) {
            System.err.println("Failed to create admin: " + createResponse.getBody());
            throw new RuntimeException("Failed to create admin: " + createResponse.getStatusCode());
        }

        // Now login with the same credentials
        AdminLoginDTO loginDTO = new AdminLoginDTO(uniqueEmail, "password123");
        HttpEntity<AdminLoginDTO> loginRequest = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            adminUrl + "/login", loginRequest, String.class);

        if (!loginResponse.getStatusCode().equals(HttpStatus.OK)) {
            System.err.println("Failed to login admin: " + loginResponse.getBody());
            throw new RuntimeException("Failed to login admin: " + loginResponse.getStatusCode());
        }

        // Extract session cookies from login response
        List<String> cookies = loginResponse.getHeaders().get("Set-Cookie");
        HttpHeaders authenticatedHeaders = new HttpHeaders();
        authenticatedHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (cookies != null && !cookies.isEmpty()) {
            authenticatedHeaders.set("Cookie", cookies.get(0));
        }

        return authenticatedHeaders;
    }

    @Test
    void testCreateStudent_Success() throws Exception {
        // Given
        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", "john.doe" + System.currentTimeMillis() + "@test.com", "Computer Science", 20);
        
        HttpEntity<StudentCreateDTO> request = new HttpEntity<>(createDTO, authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("firstName").asText()).isEqualTo("John");
        assertThat(responseBody.get("lastName").asText()).isEqualTo("Doe");
        assertThat(responseBody.get("course").asText()).isEqualTo("Computer Science");
        assertThat(responseBody.get("age").asInt()).isEqualTo(20);
        
        // Verify in database
        assertThat(studentRepository.count()).isGreaterThan(0);
    }

    @Test
    void testCreateStudent_InvalidData() {
        // Given - Invalid age
        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", "invalid-email", "Computer Science", 15); // Age below 16
        
        HttpEntity<StudentCreateDTO> request = new HttpEntity<>(createDTO, authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateStudent_EmailAlreadyExists() {
        // Given - Create first student
        String duplicateEmail = "duplicate" + System.currentTimeMillis() + "@test.com";
        Student existingStudent = new Student("Jane", "Smith", duplicateEmail, "Mathematics", 22);
        studentRepository.save(existingStudent);

        // Try to create another student with same email
        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", duplicateEmail, "Computer Science", 20);
        
        HttpEntity<StudentCreateDTO> request = new HttpEntity<>(createDTO, authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Email already exists");
    }

    @Test
    void testGetAllStudents_Success() throws Exception {
        // Given - Create test students
        String email1 = "john" + System.currentTimeMillis() + "@test.com";
        String email2 = "jane" + System.currentTimeMillis() + "@test.com";
        studentRepository.save(new Student("John", "Doe", email1, "Computer Science", 20));
        studentRepository.save(new Student("Jane", "Smith", email2, "Mathematics", 22));

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When - Note: getAllStudents doesn't require authentication based on your controller
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.isArray()).isTrue();
        assertThat(responseBody.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testGetStudentById_Success() throws Exception {
        // Given
        Student student = studentRepository.save(
            new Student("John", "Doe", "john@test.com", "Computer Science", 20));
        
        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + student.getStudentId(), HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("firstName").asText()).isEqualTo("John");
        assertThat(responseBody.get("email").asText()).isEqualTo("john@test.com");
    }

    @Test
    void testGetStudentById_NotFound() {
        // Given - Non-existent student ID
        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/999", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Student not found with ID: 999");
    }

    @Test
    void testUpdateStudent_Success() throws Exception {
        // Given - Create student to update
        Student student = studentRepository.save(
            new Student("John", "Doe", "john@test.com", "Computer Science", 20));

        StudentCreateDTO updateDTO = new StudentCreateDTO(
            "John", "Updated", "john.updated@test.com", "Mathematics", 21);
        
        HttpEntity<StudentCreateDTO> request = new HttpEntity<>(updateDTO, authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + student.getStudentId(), HttpMethod.PUT, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("lastName").asText()).isEqualTo("Updated");
        assertThat(responseBody.get("email").asText()).isEqualTo("john.updated@test.com");
        assertThat(responseBody.get("course").asText()).isEqualTo("Mathematics");
        assertThat(responseBody.get("age").asInt()).isEqualTo(21);

        // Verify in database
        Student updatedStudent = studentRepository.findById(student.getStudentId()).orElse(null);
        assertThat(updatedStudent).isNotNull();
        assertThat(updatedStudent.getLastName()).isEqualTo("Updated");
        assertThat(updatedStudent.getEmail()).isEqualTo("john.updated@test.com");
    }

    @Test
    void testDeleteStudent_Success() {
        // Given
        Student student = studentRepository.save(
            new Student("John", "Doe", "john@test.com", "Computer Science", 20));
        
        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/" + student.getStudentId(), HttpMethod.DELETE, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion in database
        assertThat(studentRepository.findById(student.getStudentId())).isEmpty();
        assertThat(studentRepository.count()).isEqualTo(0);
    }

    @Test
    void testGetStudentsByCourse_Success() throws Exception {
        // Given - Create students with different courses
        studentRepository.save(new Student("John", "Doe", "john@test.com", "Computer Science", 20));
        studentRepository.save(new Student("Jane", "Smith", "jane@test.com", "Computer Science", 22));
        studentRepository.save(new Student("Bob", "Wilson", "bob@test.com", "Mathematics", 21));

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/course/Computer Science", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.isArray()).isTrue();
        assertThat(responseBody.size()).isEqualTo(2);
        
        // Verify both students are in Computer Science
        for (JsonNode studentNode : responseBody) {
            assertThat(studentNode.get("course").asText()).isEqualTo("Computer Science");
        }
    }

    @Test
    void testGetStudentsByAgeRange_Success() throws Exception {
        // Given - Create students with different ages
        studentRepository.save(new Student("John", "Doe", "john@test.com", "Computer Science", 19));
        studentRepository.save(new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22));
        studentRepository.save(new Student("Bob", "Wilson", "bob@test.com", "Physics", 25));

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/age?minAge=20&maxAge=24", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.isArray()).isTrue();
        assertThat(responseBody.size()).isEqualTo(1); // Only Jane (22) is in range 20-24
        
        JsonNode student = responseBody.get(0);
        assertThat(student.get("firstName").asText()).isEqualTo("Jane");
        assertThat(student.get("age").asInt()).isEqualTo(22);
    }

    @Test
    void testGetAllCourses_Success() throws Exception {
        // Given - Create students with different courses
        studentRepository.save(new Student("John", "Doe", "john@test.com", "Computer Science", 20));
        studentRepository.save(new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22));
        studentRepository.save(new Student("Bob", "Wilson", "bob@test.com", "Physics", 21));
        studentRepository.save(new Student("Alice", "Brown", "alice@test.com", "Computer Science", 23)); // Duplicate course

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/courses", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.isArray()).isTrue();
        assertThat(responseBody.size()).isEqualTo(3); // Computer Science, Mathematics, Physics
        
        // Verify courses are returned (order may vary)
        String[] courses = objectMapper.treeToValue(responseBody, String[].class);
        assertThat(courses).contains("Computer Science", "Mathematics", "Physics");
    }

    @Test
    void testGetStudentCountByCourse_Success() throws Exception {
        // Given - Create students with different courses
        studentRepository.save(new Student("John", "Doe", "john@test.com", "Computer Science", 20));
        studentRepository.save(new Student("Jane", "Smith", "jane@test.com", "Computer Science", 22));
        studentRepository.save(new Student("Bob", "Wilson", "bob@test.com", "Mathematics", 21));

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/course/Computer Science/count", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("2");
    }

    @Test
    void testCheckEmailExists_Success() throws Exception {
        // Given - Create student with specific email
        studentRepository.save(new Student("John", "Doe", "john@test.com", "Computer Science", 20));

        HttpEntity<String> request = new HttpEntity<>(authenticatedHeaders);

        // When - Check existing email
        ResponseEntity<String> response1 = restTemplate.exchange(
            baseUrl + "/email/exists?email=john@test.com", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).isEqualTo("true");

        // When - Check non-existing email
        ResponseEntity<String> response2 = restTemplate.exchange(
            baseUrl + "/email/exists?email=nonexistent@test.com", HttpMethod.GET, request, String.class);

        // Then
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isEqualTo("false");
    }

    @Test
    void testCompleteStudentLifecycle() throws Exception {
        // Test complete CRUD operations in sequence
        
        // 1. Create student
        String uniqueEmail = "john.lifecycle" + System.currentTimeMillis() + "@test.com";
        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", uniqueEmail, "Computer Science", 20);
        
        HttpEntity<StudentCreateDTO> createRequest = new HttpEntity<>(createDTO, authenticatedHeaders);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            baseUrl, createRequest, String.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode createdStudent = objectMapper.readTree(createResponse.getBody());
        Long studentId = createdStudent.get("studentId").asLong();

        // 2. Read student
        HttpEntity<String> getRequest = new HttpEntity<>(authenticatedHeaders);
        ResponseEntity<String> getResponse = restTemplate.exchange(
            baseUrl + "/" + studentId, HttpMethod.GET, getRequest, String.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode retrievedStudent = objectMapper.readTree(getResponse.getBody());
        assertThat(retrievedStudent.get("email").asText()).isEqualTo(uniqueEmail);

        // 3. Update student
        String updatedEmail = "john.updated" + System.currentTimeMillis() + "@test.com";
        StudentCreateDTO updateDTO = new StudentCreateDTO(
            "John", "Updated", updatedEmail, "Mathematics", 21);
        
        HttpEntity<StudentCreateDTO> updateRequest = new HttpEntity<>(updateDTO, authenticatedHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
            baseUrl + "/" + studentId, HttpMethod.PUT, updateRequest, String.class);
        
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode updatedStudent = objectMapper.readTree(updateResponse.getBody());
        assertThat(updatedStudent.get("lastName").asText()).isEqualTo("Updated");
        assertThat(updatedStudent.get("email").asText()).isEqualTo(updatedEmail);

        // 4. Delete student
        HttpEntity<String> deleteRequest = new HttpEntity<>(authenticatedHeaders);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + studentId, HttpMethod.DELETE, deleteRequest, String.class);
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 5. Verify deletion
        ResponseEntity<String> getDeletedResponse = restTemplate.exchange(
            baseUrl + "/" + studentId, HttpMethod.GET, getRequest, String.class);
        
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDatabaseOperations() {
        // Test direct database operations to verify the student repository works
        
        // Create student directly
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student savedStudent = studentRepository.save(student);
        
        assertThat(savedStudent.getStudentId()).isNotNull();
        assertThat(savedStudent.getEmail()).isEqualTo("john@test.com");
        
        // Test findByEmail
        Student foundStudent = studentRepository.findByEmail("john@test.com").orElse(null);
        assertThat(foundStudent).isNotNull();
        assertThat(foundStudent.getFirstName()).isEqualTo("John");
        
        // Test existsByEmail
        assertThat(studentRepository.existsByEmail("john@test.com")).isTrue();
        assertThat(studentRepository.existsByEmail("nonexistent@test.com")).isFalse();
        
        // Test findByCourseIgnoreCase
        List<Student> csStudents = studentRepository.findByCourseIgnoreCase("computer science");
        assertThat(csStudents).hasSize(1);
        assertThat(csStudents.get(0).getEmail()).isEqualTo("john@test.com");
        
        // Test findByAgeBetween
        List<Student> studentsInRange = studentRepository.findByAgeBetween(18, 25);
        assertThat(studentsInRange).hasSize(1);
        
        // Test count
        assertThat(studentRepository.count()).isEqualTo(1);
        
        // Test delete
        studentRepository.deleteById(savedStudent.getStudentId());
        assertThat(studentRepository.count()).isEqualTo(0);
    }

    @Test
    void testAuthenticationFlow() throws Exception {
        // Test the complete authentication flow to ensure it works
        
        // Generate unique email to avoid conflicts with setUp method
        String uniqueTestEmail = "testauth" + System.currentTimeMillis() + "@example.com";
        
        // 1. Create admin
        AdminLoginDTO createDTO = new AdminLoginDTO(uniqueTestEmail, "testpass123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginDTO> createRequest = new HttpEntity<>(createDTO, headers);

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            adminUrl + "/create", createRequest, String.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // 2. Login
        AdminLoginDTO loginDTO = new AdminLoginDTO(uniqueTestEmail, "testpass123");
        HttpEntity<AdminLoginDTO> loginRequest = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            adminUrl + "/login", loginRequest, String.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // 3. Extract cookies and test authenticated request
        List<String> cookies = loginResponse.getHeaders().get("Set-Cookie");
        assertThat(cookies).isNotNull();
        assertThat(cookies).isNotEmpty();
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("Cookie", cookies.get(0));
        
        // 4. Test authenticated student creation
        StudentCreateDTO studentDTO = new StudentCreateDTO(
            "Auth", "Test", "auth.test" + System.currentTimeMillis() + "@test.com", "Computer Science", 20);
        
        HttpEntity<StudentCreateDTO> studentRequest = new HttpEntity<>(studentDTO, authHeaders);
        ResponseEntity<String> studentResponse = restTemplate.postForEntity(
            baseUrl, studentRequest, String.class);
        
        assertThat(studentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode responseBody = objectMapper.readTree(studentResponse.getBody());
        assertThat(responseBody.get("firstName").asText()).isEqualTo("Auth");
    }
}