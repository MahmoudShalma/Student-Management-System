package com.example.studentms.controller;

import com.example.studentms.dto.StudentCreateDTO;
import com.example.studentms.dto.StudentDTO;
import com.example.studentms.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create admin session
    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminEmail", "admin@test.com");
        return session;
    }

    @Test
    void testCreateStudent_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        StudentCreateDTO createDTO = new StudentCreateDTO("John", "Doe", "john@test.com", "Computer Science", 20);
        
        StudentDTO responseDTO = new StudentDTO();
        responseDTO.setStudentId(1L);
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setEmail("john@test.com");
        responseDTO.setCourse("Computer Science");
        responseDTO.setAge(20);
        responseDTO.setRegistrationDate(LocalDateTime.now());
        responseDTO.setLastModifiedDate(LocalDateTime.now());

        when(studentService.createStudent(any(StudentCreateDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/students")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));

        verify(studentService).createStudent(any(StudentCreateDTO.class));
    }

    @Test
    void testCreateStudent_Unauthorized() throws Exception {
        // Given - No session (not logged in)
        StudentCreateDTO createDTO = new StudentCreateDTO("John", "Doe", "john@test.com", "Computer Science", 20);

        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Admin authentication required"));

        verify(studentService, never()).createStudent(any(StudentCreateDTO.class));
    }

    @Test
    void testGetAllStudents_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        
        StudentDTO student1 = new StudentDTO();
        student1.setStudentId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        
        StudentDTO student2 = new StudentDTO();
        student2.setStudentId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");

        List<StudentDTO> students = Arrays.asList(student1, student2);
        when(studentService.getAllStudents()).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(studentService).getAllStudents();
    }

    @Test
    void testGetStudentById_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        Long studentId = 1L;
        
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setStudentId(studentId);
        studentDTO.setFirstName("John");
        studentDTO.setLastName("Doe");

        when(studentService.getStudentById(studentId)).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/api/students/{id}", studentId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(studentService).getStudentById(studentId);
    }

    @Test
    void testUpdateStudent_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        Long studentId = 1L;
        StudentCreateDTO updateDTO = new StudentCreateDTO("John", "Updated", "john.updated@test.com", "Mathematics", 21);
        
        StudentDTO responseDTO = new StudentDTO();
        responseDTO.setStudentId(studentId);
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Updated");

        when(studentService.updateStudent(eq(studentId), any(StudentCreateDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/students/{id}", studentId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(studentService).updateStudent(eq(studentId), any(StudentCreateDTO.class));
    }

    @Test
    void testDeleteStudent_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        Long studentId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/students/{id}", studentId).session(session))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(studentId);
    }

    @Test
    void testGetStudentsByCourse_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        String course = "Computer Science";
        
        StudentDTO student = new StudentDTO();
        student.setCourse(course);
        List<StudentDTO> students = Arrays.asList(student);

        when(studentService.getStudentsByCourse(course)).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students/course/{course}", course).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].course").value(course));

        verify(studentService).getStudentsByCourse(course);
    }

    @Test
    void testGetStudentsByAgeRange_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        Integer minAge = 18;
        Integer maxAge = 25;
        
        StudentDTO student = new StudentDTO();
        student.setAge(20);
        List<StudentDTO> students = Arrays.asList(student);

        when(studentService.getStudentsByAgeRange(minAge, maxAge)).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students/age")
                .session(session)
                .param("minAge", minAge.toString())
                .param("maxAge", maxAge.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(studentService).getStudentsByAgeRange(minAge, maxAge);
    }

    @Test
    void testGetAllCourses_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        List<String> courses = Arrays.asList("Computer Science", "Mathematics", "Physics");

        when(studentService.getAllCourses()).thenReturn(courses);

        // When & Then
        mockMvc.perform(get("/api/students/courses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Computer Science"));

        verify(studentService).getAllCourses();
    }

    @Test
    void testCheckEmailExists_Success() throws Exception {
        // Given
        MockHttpSession session = createAdminSession();
        String email = "test@example.com";

        when(studentService.existsByEmail(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/students/email/exists")
                .session(session)
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(studentService).existsByEmail(email);
    }
}