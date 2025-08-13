package com.example.studentms.dto;

import java.time.LocalDateTime;

public class StudentDTO {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String course;
    private Integer age;
    private LocalDateTime registrationDate;
    private LocalDateTime lastModifiedDate;

    // Constructors
    public StudentDTO() {}

    public StudentDTO(Long studentId, String firstName, String lastName, String email, 
                     String course, Integer age, LocalDateTime registrationDate, 
                     LocalDateTime lastModifiedDate) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.course = course;
        this.age = age;
        this.registrationDate = registrationDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}