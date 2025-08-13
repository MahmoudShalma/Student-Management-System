package com.example.studentms.controller;

import java.util.List;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/students")
@CrossOrigin(origins = "*")  // Allow requests from any origin 
public class StudentController {
    
    private final StudentService studentService;

    // Constructor injection for StudentService
    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

  // Create a new student
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentCreateDTO studentCreateDTO) {
        StudentDTO createdStudent = studentService.createStudent(studentCreateDTO);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    // Get all students
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    // Update student
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, 
                                                   @Valid @RequestBody StudentCreateDTO studentCreateDTO) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentCreateDTO);
        return ResponseEntity.ok(updatedStudent);
    }

   // Delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // Get students by course
    @GetMapping("/course/{course}")
    public ResponseEntity<List<StudentDTO>> getStudentsByCourse(@PathVariable String course) {
        List<StudentDTO> students = studentService.getStudentsByCourse(course);
        return ResponseEntity.ok(students);
    }

    // Get students by age range
    @GetMapping("/age")
    public ResponseEntity<List<StudentDTO>> getStudentsByAgeRange(
            @RequestParam Integer minAge, 
            @RequestParam Integer maxAge) {
        List<StudentDTO> students = studentService.getStudentsByAgeRange(minAge, maxAge);
        return ResponseEntity.ok(students);
    }

   // Get all courses
    @GetMapping("/courses")
    public ResponseEntity<List<String>> getAllCourses() {
        List<String> courses = studentService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

       // Get student count by course
    @GetMapping("/course/{course}/count")
    public ResponseEntity<Long> getStudentCountByCourse(@PathVariable String course) {
        Long count = studentService.getStudentCountByCourse(course);
        return ResponseEntity.ok(count);
    }

   // Check if email exists
    @GetMapping("/email/exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = studentService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    }
