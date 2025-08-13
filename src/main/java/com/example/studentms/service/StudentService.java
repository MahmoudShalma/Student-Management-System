package com.example.studentms.service;

import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.dto.StudentDTO;

import java.util.List;

public interface StudentService {
    
    StudentDTO createStudent(StudentCreateDTO studentCreateDTO);
    
    StudentDTO getStudentById(Long studentId);
    
    List<StudentDTO> getAllStudents();
    
    StudentDTO updateStudent(Long studentId, StudentCreateDTO studentCreateDTO);
    
    void deleteStudent(Long studentId);
    
    List<StudentDTO> getStudentsByCourse(String course);
    
    List<StudentDTO> getStudentsByAgeRange(Integer minAge, Integer maxAge);
    
    List<String> getAllCourses();
    
    Long getStudentCountByCourse(String course);
    
    boolean existsByEmail(String email);
}