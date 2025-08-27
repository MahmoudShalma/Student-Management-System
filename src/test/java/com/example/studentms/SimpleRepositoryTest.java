package com.example.studentms;

import com.example.studentms.model.Admin;
import com.example.studentms.model.Student;
import com.example.studentms.repository.AdminRepository;
import com.example.studentms.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SimpleRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testSaveAdmin() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        
        // When
        Admin savedAdmin = adminRepository.save(admin);
        
        // Then
        assertThat(savedAdmin).isNotNull();
        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@test.com");
        assertThat(savedAdmin.getPassword()).isEqualTo("password123");
    }

    @Test
    void testSaveStudent() {
        // Given
        Student student = new Student("John", "Doe", "john.doe@test.com", "Computer Science", 20);
        
        // When
        Student savedStudent = studentRepository.save(student);
        
        // Then
        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getStudentId()).isNotNull();
        assertThat(savedStudent.getFirstName()).isEqualTo("John");
        assertThat(savedStudent.getLastName()).isEqualTo("Doe");
        assertThat(savedStudent.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(savedStudent.getCourse()).isEqualTo("Computer Science");
        assertThat(savedStudent.getAge()).isEqualTo(20);
    }

    @Test
    void testFindAdminByEmail() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);
        
        // When
        Admin foundAdmin = adminRepository.findByEmail("admin@test.com").orElse(null);
        
        // Then
        assertThat(foundAdmin).isNotNull();
        assertThat(foundAdmin.getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void testFindStudentByEmail() {
        // Given
        Student student = new Student("Jane", "Smith", "jane.smith@test.com", "Mathematics", 21);
        studentRepository.save(student);
        
        // When
        Student foundStudent = studentRepository.findByEmail("jane.smith@test.com").orElse(null);
        
        // Then
        assertThat(foundStudent).isNotNull();
        assertThat(foundStudent.getEmail()).isEqualTo("jane.smith@test.com");
    }

    @Test
    void testExistsByEmail() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);
        
        // When & Then
        assertThat(adminRepository.existsByEmail("admin@test.com")).isTrue();
        assertThat(adminRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }
}