package com.example.studentms.service;

import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.dto.StudentDTO;
import com.example.studentms.exception.EmailAlreadyExistsException;
import com.example.studentms.exception.StudentNotFoundException;
import com.example.studentms.model.Student;
import com.example.studentms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentDTO createStudent(StudentCreateDTO studentCreateDTO) {
        // Check if email already exists
        if (studentRepository.existsByEmail(studentCreateDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + studentCreateDTO.getEmail());
        }

        Student student = new Student(
            studentCreateDTO.getFirstName(),
            studentCreateDTO.getLastName(),
            studentCreateDTO.getEmail(),
            studentCreateDTO.getCourse(),
            studentCreateDTO.getAge()
        );

        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));
        return convertToDTO(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO updateStudent(Long studentId, StudentCreateDTO studentCreateDTO) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        // Check if email is being changed and if new email already exists
        if (!existingStudent.getEmail().equals(studentCreateDTO.getEmail()) &&
            studentRepository.existsByEmail(studentCreateDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + studentCreateDTO.getEmail());
        }

        existingStudent.setFirstName(studentCreateDTO.getFirstName());
        existingStudent.setLastName(studentCreateDTO.getLastName());
        existingStudent.setEmail(studentCreateDTO.getEmail());
        existingStudent.setCourse(studentCreateDTO.getCourse());
        existingStudent.setAge(studentCreateDTO.getAge());

        Student updatedStudent = studentRepository.save(existingStudent);
        return convertToDTO(updatedStudent);
    }

    @Override
    public void deleteStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student not found with ID: " + studentId);
        }
        studentRepository.deleteById(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsByCourse(String course) {
        return studentRepository.findByCourseIgnoreCase(course)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsByAgeRange(Integer minAge, Integer maxAge) {
        return studentRepository.findByAgeBetween(minAge, maxAge)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCourses() {
        return studentRepository.findAllDistinctCourses();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getStudentCountByCourse(String course) {
        return studentRepository.countStudentsByCourse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    // Helper method to convert Student entity to StudentDTO
    // This method is used to avoid code duplication
    // when converting entities to DTOs in multiple service methods
    private StudentDTO convertToDTO(Student student) {
        return new StudentDTO(
            student.getStudentId(),
            student.getFirstName(),
            student.getLastName(),
            student.getEmail(),
            student.getCourse(),
            student.getAge(),
            student.getRegistrationDate(),
            student.getLastModifiedDate()
        );
    }
}