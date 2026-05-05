package com.employee.employee_system.Mapper;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.entity.Employee;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


@Component
public class EmployeeMapper {

    // Called by Service before returning data to Controller
    public EmployeeResponseDto toResponseDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        BeanUtils.copyProperties(e, dto);
        return dto;
    }

    // Called by Service before saving to DB
    public Employee toEntity(EmployeeRequestDto dto) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(dto, employee);
        // id, createdAt, updatedAt are NOT in the DTO — DB/JPA handles them
        if (employee.getActive() == null) {
            employee.setActive(true);
        }
        return employee;
    }

    // Applies changes from RequestDto onto an EXISTING entity
    // Used by PUT (full update)
    public void updateEntityFromDto(EmployeeRequestDto dto, Employee e) {
        BeanUtils.copyProperties(dto, e);
        // @PreUpdate will fire automatically and set updatedAt
    }
}