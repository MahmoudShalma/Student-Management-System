package com.example.studentms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentmsApplication.class, args);
		System.out.println("Student Management System Application Started Successfully!");
		System.out.println("Visit: http://localhost:8080 for the application interface.");
	}

}
