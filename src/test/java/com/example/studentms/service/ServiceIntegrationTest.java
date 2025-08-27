package com.example.studentms.service;

import com.example.studentms.dto.AdminLoginDTO;
import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.dto.StudentDTO;
import com.example.studentms.exception.EmailAlreadyExistsException;
import com.example.studentms.exception.StudentNotFoundException;
import com.example.studentms.model.Admin;
import com.example.studentms.model.Student;
import com.example.studentms.repository.AdminRepository;
import com.example.studentms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for service layer
 * Tests the complete service layer functionality with real database interaction
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never"
})
@Transactional
class ServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        // Clean repositories before each test
        studentRepository.deleteAll();
        adminRepository.deleteAll();
    }

    @Test
    void testStudentService_CreateStudent_Success() {
        // Given
        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", "john@test.com", "Computer Science", 20
        );

        // When
        StudentDTO createdStudent = studentService.createStudent(createDTO);

        // Then
        assertThat(createdStudent).isNotNull();
        assertThat(createdStudent.getStudentId()).isNotNull();
        assertThat(createdStudent.getFirstName()).isEqualTo("John");
        assertThat(createdStudent.getLastName()).isEqualTo("Doe");
        assertThat(createdStudent.getEmail()).isEqualTo("john@test.com");
        assertThat(createdStudent.getCourse()).isEqualTo("Computer Science");
        assertThat(createdStudent.getAge()).isEqualTo(20);
        assertThat(createdStudent.getRegistrationDate()).isNotNull();
        assertThat(createdStudent.getLastModifiedDate()).isNotNull();

        // Verify in database
        assertThat(studentRepository.count()).isEqualTo(1);
        assertThat(studentRepository.existsByEmail("john@test.com")).isTrue();
    }

    @Test
    void testStudentService_CreateStudent_EmailAlreadyExists() {
        // Given - Create existing student
        Student existingStudent = new Student("Jane", "Smith", "duplicate@test.com", "Mathematics", 22);
        studentRepository.save(existingStudent);

        StudentCreateDTO createDTO = new StudentCreateDTO(
            "John", "Doe", "duplicate@test.com", "Computer Science", 20
        );

        // When & Then
        assertThatThrownBy(() -> studentService.createStudent(createDTO))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("Email already exists: duplicate@test.com");

        // Verify only one student exists
        assertThat(studentRepository.count()).isEqualTo(1);
    }

    @Test
    void testStudentService_GetStudentById_Success() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student savedStudent = studentRepository.save(student);

        // When
        StudentDTO foundStudent = studentService.getStudentById(savedStudent.getStudentId());

        // Then
        assertThat(foundStudent).isNotNull();
        assertThat(foundStudent.getStudentId()).isEqualTo(savedStudent.getStudentId());
        assertThat(foundStudent.getFirstName()).isEqualTo("John");
        assertThat(foundStudent.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void testStudentService_GetStudentById_NotFound() {
        // When & Then
        assertThatThrownBy(() -> studentService.getStudentById(999L))
            .isInstanceOf(StudentNotFoundException.class)
            .hasMessageContaining("Student not found with ID: 999");
    }

    @Test
    void testStudentService_GetAllStudents() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Physics", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));

        // When
        List<StudentDTO> allStudents = studentService.getAllStudents();

        // Then
        assertThat(allStudents).hasSize(3);
        assertThat(allStudents).extracting("firstName")
            .containsExactlyInAnyOrder("John", "Jane", "Bob");
    }

    @Test
    void testStudentService_UpdateStudent_Success() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student savedStudent = studentRepository.save(student);

        StudentCreateDTO updateDTO = new StudentCreateDTO(
            "John", "Updated", "john.updated@test.com", "Mathematics", 21
        );

        // When
        StudentDTO updatedStudent = studentService.updateStudent(savedStudent.getStudentId(), updateDTO);

        // Then
        assertThat(updatedStudent.getLastName()).isEqualTo("Updated");
        assertThat(updatedStudent.getEmail()).isEqualTo("john.updated@test.com");
        assertThat(updatedStudent.getCourse()).isEqualTo("Mathematics");
        assertThat(updatedStudent.getAge()).isEqualTo(21);

        // Verify in database
        Student dbStudent = studentRepository.findById(savedStudent.getStudentId()).orElse(null);
        assertThat(dbStudent).isNotNull();
        assertThat(dbStudent.getLastName()).isEqualTo("Updated");
    }

    @Test
    void testStudentService_UpdateStudent_EmailAlreadyExists() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        studentRepository.saveAll(List.of(student1, student2));

        StudentCreateDTO updateDTO = new StudentCreateDTO(
            "John", "Doe", "jane@test.com", "Computer Science", 20 // Trying to use Jane's email
        );

        // When & Then
        assertThatThrownBy(() -> studentService.updateStudent(student1.getStudentId(), updateDTO))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("Email already exists: jane@test.com");
    }

    @Test
    void testStudentService_DeleteStudent_Success() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student savedStudent = studentRepository.save(student);

        // When
        studentService.deleteStudent(savedStudent.getStudentId());

        // Then
        assertThat(studentRepository.existsById(savedStudent.getStudentId())).isFalse();
        assertThat(studentRepository.count()).isEqualTo(0);
    }

    @Test
    void testStudentService_DeleteStudent_NotFound() {
        // When & Then
        assertThatThrownBy(() -> studentService.deleteStudent(999L))
            .isInstanceOf(StudentNotFoundException.class)
            .hasMessageContaining("Student not found with ID: 999");
    }

    @Test
    void testStudentService_GetStudentsByCourse() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "computer science", 22); // Different case
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Mathematics", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));

        // When
        List<StudentDTO> csStudents = studentService.getStudentsByCourse("Computer Science");

        // Then
        assertThat(csStudents).hasSize(2);
        assertThat(csStudents).extracting("firstName")
            .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void testStudentService_GetStudentsByAgeRange() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 19);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Physics", 25);
        
        studentRepository.saveAll(List.of(student1, student2, student3));

        // When
        List<StudentDTO> studentsInRange = studentService.getStudentsByAgeRange(20, 23);

        // Then
        assertThat(studentsInRange).hasSize(1);
        assertThat(studentsInRange.get(0).getFirstName()).isEqualTo("Jane");
        assertThat(studentsInRange.get(0).getAge()).isEqualTo(22);
    }

    @Test
    void testStudentService_GetAllCourses() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Computer Science", 21);
        Student student4 = new Student("Alice", "Johnson", "alice@test.com", "Physics", 23);
        
        studentRepository.saveAll(List.of(student1, student2, student3, student4));

        // When
        List<String> courses = studentService.getAllCourses();

        // Then
        assertThat(courses).hasSize(3);
        assertThat(courses).containsExactlyInAnyOrder("Computer Science", "Mathematics", "Physics");
    }

    @Test
    void testStudentService_GetStudentCountByCourse() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Computer Science", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Mathematics", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));

        // When
        Long csCount = studentService.getStudentCountByCourse("Computer Science");
        Long mathCount = studentService.getStudentCountByCourse("Mathematics");
        Long physicsCount = studentService.getStudentCountByCourse("Physics");

        // Then
        assertThat(csCount).isEqualTo(2);
        assertThat(mathCount).isEqualTo(1);
        assertThat(physicsCount).isEqualTo(0);
    }

    @Test
    void testStudentService_ExistsByEmail() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        studentRepository.save(student);

        // When & Then
        assertThat(studentService.existsByEmail("john@test.com")).isTrue();
        assertThat(studentService.existsByEmail("nonexistent@test.com")).isFalse();
    }

    @Test
    void testAdminService_CreateAdmin_Success() {
        // Given
        String email = "admin@test.com";
        String password = "password123";

        // When
        Admin createdAdmin = adminService.createAdmin(email, password);

        // Then
        assertThat(createdAdmin).isNotNull();
        assertThat(createdAdmin.getId()).isNotNull();
        assertThat(createdAdmin.getEmail()).isEqualTo(email);
        assertThat(createdAdmin.getPassword()).isEqualTo(password);

        // Verify in database
        assertThat(adminRepository.count()).isEqualTo(1);
        assertThat(adminRepository.existsByEmail(email)).isTrue();
    }

    @Test
    void testAdminService_CreateAdmin_EmailAlreadyExists() {
        // Given
        Admin existingAdmin = new Admin("admin@test.com", "password123");
        adminRepository.save(existingAdmin);

        // When & Then
        assertThatThrownBy(() -> adminService.createAdmin("admin@test.com", "newpassword"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Admin with this email already exists");

        // Verify only one admin exists
        assertThat(adminRepository.count()).isEqualTo(1);
    }

    @Test
    void testAdminService_ValidateAdmin_Success() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);

        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "password123");

        // When
        boolean isValid = adminService.validateAdmin(loginDTO);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testAdminService_ValidateAdmin_WrongPassword() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);

        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "wrongpassword");

        // When
        boolean isValid = adminService.validateAdmin(loginDTO);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testAdminService_ValidateAdmin_NonExistentEmail() {
        // Given
        AdminLoginDTO loginDTO = new AdminLoginDTO("nonexistent@test.com", "password123");

        // When
        boolean isValid = adminService.validateAdmin(loginDTO);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testCompleteWorkflow_StudentManagementWithValidation() {
        // Step 1: Create admin
        Admin admin = adminService.createAdmin("admin@test.com", "password123");
        assertThat(admin).isNotNull();

        // Step 2: Validate admin login
        AdminLoginDTO loginDTO = new AdminLoginDTO("admin@test.com", "password123");
        assertThat(adminService.validateAdmin(loginDTO)).isTrue();

        // Step 3: Create students
        StudentCreateDTO student1DTO = new StudentCreateDTO(
            "John", "Doe", "john@test.com", "Computer Science", 20
        );
        StudentCreateDTO student2DTO = new StudentCreateDTO(
            "Jane", "Smith", "jane@test.com", "Mathematics", 22
        );

        StudentDTO createdStudent1 = studentService.createStudent(student1DTO);
        StudentDTO createdStudent2 = studentService.createStudent(student2DTO);

        assertThat(createdStudent1.getStudentId()).isNotNull();
        assertThat(createdStudent2.getStudentId()).isNotNull();

        // Step 4: Test various queries
        List<StudentDTO> allStudents = studentService.getAllStudents();
        assertThat(allStudents).hasSize(2);

        List<StudentDTO> csStudents = studentService.getStudentsByCourse("Computer Science");
        assertThat(csStudents).hasSize(1);

        List<String> courses = studentService.getAllCourses();
        assertThat(courses).hasSize(2);

        // Step 5: Update student
        StudentCreateDTO updateDTO = new StudentCreateDTO(
            "John", "Updated", "john.updated@test.com", "Physics", 21
        );
        StudentDTO updatedStudent = studentService.updateStudent(createdStudent1.getStudentId(), updateDTO);
        assertThat(updatedStudent.getLastName()).isEqualTo("Updated");
        assertThat(updatedStudent.getCourse()).isEqualTo("Physics");

        // Step 6: Delete student
        studentService.deleteStudent(createdStudent2.getStudentId());
        assertThat(studentRepository.count()).isEqualTo(1);

        // Step 7: Final verification
        List<StudentDTO> finalStudents = studentService.getAllStudents();
        assertThat(finalStudents).hasSize(1);
        assertThat(finalStudents.get(0).getLastName()).isEqualTo("Updated");
    }

    @Test
    void testTransactionalBehavior_RollbackOnException() {
        // Given
        Student existingStudent = new Student("Jane", "Smith", "existing@test.com", "Mathematics", 22);
        studentRepository.save(existingStudent);

        StudentCreateDTO invalidDTO = new StudentCreateDTO(
            "John", "Doe", "existing@test.com", "Computer Science", 20 // Duplicate email
        );

        // When & Then
        assertThatThrownBy(() -> studentService.createStudent(invalidDTO))
            .isInstanceOf(EmailAlreadyExistsException.class);

        // Verify transaction was rolled back - only original student exists
        assertThat(studentRepository.count()).isEqualTo(1);
        assertThat(studentRepository.findByEmail("existing@test.com")).isPresent();
    }
}