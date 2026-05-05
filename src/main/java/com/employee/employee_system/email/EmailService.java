package com.employee.employee_system.email;

import com.employee.employee_system.Dto.EmployeeResponseDto;

public interface EmailService {

    void sendWelcomeEmail(EmployeeResponseDto employee);
}
