package com.example.studentms.repository;

import com.example.studentms.model.Admin;
import com.example.studentms.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for repository layer
 * Tests the interaction between repository interfaces and the database
 */
@DataJpaTest
@TestPropertySource(properties = { 
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
}) // Ensure a fresh schema for each test run
class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        studentRepository.deleteAll();
        adminRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testStudentRepository_SaveAndFindById() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);

        // When
        Student savedStudent = studentRepository.save(student);
        entityManager.flush();
        Optional<Student> foundStudent = studentRepository.findById(savedStudent.getStudentId());

        // Then
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getFirstName()).isEqualTo("John");
        assertThat(foundStudent.get().getLastName()).isEqualTo("Doe");
        assertThat(foundStudent.get().getEmail()).isEqualTo("john@test.com");
        assertThat(foundStudent.get().getRegistrationDate()).isNotNull();
        assertThat(foundStudent.get().getLastModifiedDate()).isNotNull();
    }

    @Test
    void testStudentRepository_FindByEmail() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        studentRepository.save(student);
        entityManager.flush();

        // When
        Optional<Student> foundStudent = studentRepository.findByEmail("john@test.com");
        Optional<Student> notFoundStudent = studentRepository.findByEmail("nonexistent@test.com");

        // Then
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getFirstName()).isEqualTo("John");
        assertThat(notFoundStudent).isEmpty();
    }

    @Test
    void testStudentRepository_ExistsByEmail() {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        studentRepository.save(student);
        entityManager.flush();

        // When & Then
        assertThat(studentRepository.existsByEmail("john@test.com")).isTrue();
        assertThat(studentRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }

    @Test
    void testStudentRepository_FindByCourseIgnoreCase() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "computer science", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Mathematics", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));
        entityManager.flush();

        // When
        List<Student> csStudents = studentRepository.findByCourseIgnoreCase("Computer Science");
        List<Student> mathStudents = studentRepository.findByCourseIgnoreCase("mathematics");

        // Then
        assertThat(csStudents).hasSize(2);
        assertThat(mathStudents).hasSize(1);
        assertThat(mathStudents.get(0).getFirstName()).isEqualTo("Bob");
    }

    @Test
    void testStudentRepository_FindByAgeBetween() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 19);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Physics", 25);
        
        studentRepository.saveAll(List.of(student1, student2, student3));
        entityManager.flush();

        // When
        List<Student> youngStudents = studentRepository.findByAgeBetween(18, 21);
        List<Student> olderStudents = studentRepository.findByAgeBetween(22, 26);

        // Then
        assertThat(youngStudents).hasSize(1);
        assertThat(youngStudents.get(0).getFirstName()).isEqualTo("John");
        assertThat(olderStudents).hasSize(2);
    }

    @Test
    void testStudentRepository_FindByFirstNameAndLastName() {
        // Given
        Student student1 = new Student("john", "doe", "john1@test.com", "Computer Science", 20);
        Student student2 = new Student("John", "Doe", "john2@test.com", "Mathematics", 22);
        Student student3 = new Student("Jane", "Doe", "jane@test.com", "Physics", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));
        entityManager.flush();

        // When
        List<Student> johnDoes = studentRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("John", "Doe");

        // Then
        assertThat(johnDoes).hasSize(2);
    }

    @Test
    void testStudentRepository_CustomQuery_FindStudentsByCourseAndMinAge() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 19);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Computer Science", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Computer Science", 17);
        
        studentRepository.saveAll(List.of(student1, student2, student3));
        entityManager.flush();

        // When
        List<Student> eligibleStudents = studentRepository.findStudentsByCourseAndMinAge("Computer Science", 18);

        // Then
        assertThat(eligibleStudents).hasSize(2);
        assertThat(eligibleStudents).extracting("firstName").containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void testStudentRepository_FindAllDistinctCourses() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Computer Science", 21);
        Student student4 = new Student("Alice", "Johnson", "alice@test.com", "Physics", 23);
        
        studentRepository.saveAll(List.of(student1, student2, student3, student4));
        entityManager.flush();

        // When
        List<String> courses = studentRepository.findAllDistinctCourses();

        // Then
        assertThat(courses).hasSize(3);
        assertThat(courses).containsExactlyInAnyOrder("Computer Science", "Mathematics", "Physics");
    }

    @Test
    void testStudentRepository_CountStudentsByCourse() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        Student student3 = new Student("Bob", "Wilson", "bob@test.com", "Computer Science", 21);
        
        studentRepository.saveAll(List.of(student1, student2, student3));
        entityManager.flush();

        // When
        Long csCount = studentRepository.countStudentsByCourse("Computer Science");
        Long mathCount = studentRepository.countStudentsByCourse("Mathematics");
        Long physicsCount = studentRepository.countStudentsByCourse("Physics");

        // Then
        assertThat(csCount).isEqualTo(2);
        assertThat(mathCount).isEqualTo(1);
        assertThat(physicsCount).isEqualTo(0);
    }

    @Test
    void testStudentRepository_TimestampFields() throws InterruptedException {
        // Given
        Student student = new Student("John", "Doe", "john@test.com", "Computer Science", 20);

        // When - Save initial student
        Student savedStudent = studentRepository.save(student);
        entityManager.flush();
        
        LocalDateTime initialRegistrationDate = savedStudent.getRegistrationDate();
        LocalDateTime initialModifiedDate = savedStudent.getLastModifiedDate();

        // Wait a bit and update
        Thread.sleep(100);
        savedStudent.setAge(21);
        Student updatedStudent = studentRepository.save(savedStudent);
        entityManager.flush();

        // Then
        assertThat(updatedStudent.getRegistrationDate()).isEqualTo(initialRegistrationDate);
        assertThat(updatedStudent.getLastModifiedDate()).isAfter(initialModifiedDate);
    }

    @Test
    void testAdminRepository_SaveAndFindByEmail() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");

        // When
        Admin savedAdmin = adminRepository.save(admin);
        entityManager.flush();
        Optional<Admin> foundAdmin = adminRepository.findByEmail("admin@test.com");

        // Then
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getEmail()).isEqualTo("admin@test.com");
        assertThat(foundAdmin.get().getPassword()).isEqualTo("password123");
        assertThat(foundAdmin.get().getId()).isEqualTo(savedAdmin.getId());
    }

    @Test
    void testAdminRepository_ExistsByEmail() {
        // Given
        Admin admin = new Admin("admin@test.com", "password123");
        adminRepository.save(admin);
        entityManager.flush();

        // When & Then
        assertThat(adminRepository.existsByEmail("admin@test.com")).isTrue();
        assertThat(adminRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }

    @Test
    void testCascadeOperations() {
        // Given
        Student student1 = new Student("John", "Doe", "john@test.com", "Computer Science", 20);
        Student student2 = new Student("Jane", "Smith", "jane@test.com", "Mathematics", 22);
        
        studentRepository.saveAll(List.of(student1, student2));
        entityManager.flush();
        
        assertThat(studentRepository.count()).isEqualTo(2);

        // When - Delete all
        studentRepository.deleteAll();
        entityManager.flush();

        // Then
        assertThat(studentRepository.count()).isEqualTo(0);
    }
}