package com.employee.employee_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EmployeeSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeSystemApplication.class, args);
	}

}
