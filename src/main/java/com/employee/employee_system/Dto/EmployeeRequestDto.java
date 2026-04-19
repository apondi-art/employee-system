package com.employee.employee_system.Dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@NoArgsConstructor @AllArgsConstructor
@Data
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be max 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be max 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format e.g. user@domain.com")
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.00", message = "Salary cannot be negative")
    private BigDecimal salary;

    @NotNull(message = "Date of joining is required")
    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate dateOfJoining;

    // If client omits this field, it defaults to true
    private Boolean active = true;
}
