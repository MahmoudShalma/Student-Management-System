package com.example.studentms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.dto.StudentDTO;
import com.example.studentms.service.StudentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    
    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    
    // Helper method to check if admin is logged in
    private ResponseEntity<?> checkAdminAuth(HttpSession session) {
        String adminEmail = (String) session.getAttribute("adminEmail");
        if (adminEmail == null) {
            logger.warn("Unauthorized access attempt - no admin session");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Admin authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return null; // Admin is authenticated
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentCreateDTO studentCreateDTO, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.info("Admin {} creating student with email: {}", adminEmail, studentCreateDTO.getEmail());
        
        StudentDTO createdStudent = studentService.createStudent(studentCreateDTO);
        
        logger.info("Student created successfully with ID: {} by admin: {}", createdStudent.getStudentId(), adminEmail);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents(HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} fetching all students", adminEmail);
        
        List<StudentDTO> students = studentService.getAllStudents();
        
        logger.info("Admin {} retrieved {} students", adminEmail, students.size());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} fetching student with ID: {}", adminEmail, id);
        
        StudentDTO student = studentService.getStudentById(id);
        
        logger.info("Admin {} retrieved student: {} {}", adminEmail, student.getFirstName(), student.getLastName());
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, 
                                         @Valid @RequestBody StudentCreateDTO studentCreateDTO, 
                                         HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.info("Admin {} updating student with ID: {}", adminEmail, id);
        
        StudentDTO updatedStudent = studentService.updateStudent(id, studentCreateDTO);
        
        logger.info("Student ID: {} updated successfully by admin: {}", id, adminEmail);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.warn("Admin {} deleting student with ID: {}", adminEmail, id);
        
        studentService.deleteStudent(id);
        
        logger.warn("Student ID: {} deleted by admin: {}", id, adminEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{course}")
    public ResponseEntity<?> getStudentsByCourse(@PathVariable String course, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} searching students by course: {}", adminEmail, course);
        
        List<StudentDTO> students = studentService.getStudentsByCourse(course);
        
        logger.info("Admin {} found {} students in course: {}", adminEmail, students.size(), course);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age")
    public ResponseEntity<?> getStudentsByAgeRange(@RequestParam Integer minAge, 
                                                  @RequestParam Integer maxAge, 
                                                  HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} searching students by age range: {}-{}", adminEmail, minAge, maxAge);
        
        List<StudentDTO> students = studentService.getStudentsByAgeRange(minAge, maxAge);
        
        logger.info("Admin {} found {} students in age range {}-{}", adminEmail, students.size(), minAge, maxAge);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses(HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} fetching all courses", adminEmail);
        
        List<String> courses = studentService.getAllCourses();
        
        logger.info("Admin {} retrieved {} courses", adminEmail, courses.size());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course/{course}/count")
    public ResponseEntity<?> getStudentCountByCourse(@PathVariable String course, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} getting student count for course: {}", adminEmail, course);
        
        Long count = studentService.getStudentCountByCourse(course);
        
        logger.info("Admin {} found {} students in course: {}", adminEmail, count, course);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/email/exists")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email, HttpSession session) {
        ResponseEntity<?> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        String adminEmail = (String) session.getAttribute("adminEmail");
        logger.debug("Admin {} checking if email exists: {}", adminEmail, email);
        
        boolean exists = studentService.existsByEmail(email);
        
        logger.info("Admin {} checked email {} - exists: {}", adminEmail, email, exists);
        return ResponseEntity.ok(exists);
    }
}