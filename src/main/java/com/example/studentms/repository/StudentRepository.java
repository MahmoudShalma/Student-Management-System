package com.example.studentms.repository;

import com.example.studentms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Find by email
    Optional<Student> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find by course
    List<Student> findByCourseIgnoreCase(String course);
    
    // Find by age range
    List<Student> findByAgeBetween(Integer minAge, Integer maxAge);
    
    // Find by first name and last name (case insensitive)
    List<Student> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
    
    // Custom query to find students by course and age
    @Query("SELECT s FROM Student s WHERE s.course = :course AND s.age >= :minAge")
    List<Student> findStudentsByCourseAndMinAge(@Param("course") String course, @Param("minAge") Integer minAge);
    
    // Find all courses (distinct)
    @Query("SELECT DISTINCT s.course FROM Student s ORDER BY s.course")
    List<String> findAllDistinctCourses();
    
    // Count students by course
    @Query("SELECT COUNT(s) FROM Student s WHERE s.course = :course")
    Long countStudentsByCourse(@Param("course") String course);
}