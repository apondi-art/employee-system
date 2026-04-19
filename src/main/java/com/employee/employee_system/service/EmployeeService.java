package com.employee.employee_system.service;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EmployeeService {

    EmployeeResponseDto create(EmployeeRequestDto dto);

    EmployeeResponseDto findById(Long id);

    Page<EmployeeResponseDto> findAll(
            String department, Boolean active, Pageable pageable);

    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);

    EmployeeResponseDto partialUpdate(Long id,
                                      Map<String, Object> fields);

    // Soft delete — sets active = false, row stays in DB
    void softDelete(Long id);

    // Hard delete — permanently removes (only if already inactive)
    void hardDelete(Long id);

    List<EmployeeResponseDto> findBySalaryRange(
            BigDecimal min, BigDecimal max);

    // Used by Excel/PDF export
    List<Employee> findFilteredEntities(
            String department, Boolean active);
}