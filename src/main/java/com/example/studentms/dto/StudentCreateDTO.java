package com.example.studentms.dto;

import jakarta.validation.constraints.*;

// 
// DTO for creating a new student
// This class is used to transfer data from the client to the server when creating a new student
// It contains validation annotations to ensure the data is valid before processing
//
public class StudentCreateDTO {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address should be valid")
    private String email;

    @NotBlank(message = "Course is required")
    @Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
    private String course;

    @NotNull(message = "Age is required")
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;

    // Constructors
    public StudentCreateDTO() {}

    public StudentCreateDTO(String firstName, String lastName, String email, String course, Integer age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.course = course;
        this.age = age;
    }

    // Getters and Setters
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
}