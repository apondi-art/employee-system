package com.employee.employee_system.Mapper;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.entity.Employee;
import org.springframework.stereotype.Component;


@Component  // Spring-managed bean, can be @Autowired anywhere
public class EmployeeMapper {

    // Called by Service before returning data to Controller
    public EmployeeResponseDto toResponseDto(Employee e) {
        return EmployeeResponseDto.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .department(e.getDepartment())
                .salary(e.getSalary())
                .dateOfJoining(e.getDateOfJoining())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // Called by Service before saving to DB
    public Employee toEntity(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .salary(dto.getSalary())
                .dateOfJoining(dto.getDateOfJoining())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        // id, createdAt, updatedAt are NOT set here — DB/JPA handles them
    }

    // Applies changes from RequestDto onto an EXISTING entity
    // Used by PUT (full update)
    public void updateEntityFromDto(EmployeeRequestDto dto, Employee e) {
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmail(dto.getEmail());
        e.setDepartment(dto.getDepartment());
        e.setSalary(dto.getSalary());
        e.setDateOfJoining(dto.getDateOfJoining());
        e.setActive(dto.getActive());
        // @PreUpdate will fire automatically and set updatedAt
    }
}