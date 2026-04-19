package com.employee.employee_system.Exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
