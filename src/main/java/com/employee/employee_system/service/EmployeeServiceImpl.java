package com.employee.employee_system.service;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.Exception.DuplicateEmailException;
import com.employee.employee_system.Exception.EmployeeNotFoundException;
import com.employee.employee_system.Mapper.EmployeeMapper;
import com.employee.employee_system.entity.Employee;
import com.employee.employee_system.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Validated      // activates Bean Validation on method params
@Transactional  // every method runs inside a DB transaction
@RequiredArgsConstructor // Lombok: injects via constructor (best practice)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;
    private final EmployeeMapper mapper;

    // ── CREATE ──────────────────────────────────────────────
    @Override
    public EmployeeResponseDto create(@Valid EmployeeRequestDto dto) {

        // Business Rule 1: no duplicate emails
        repo.findByEmail(dto.getEmail()).ifPresent(existing -> {
            throw new DuplicateEmailException(dto.getEmail());
        });

        // Business Rule 2: salary floor per department
        validateSalaryFloor(dto.getDepartment(), dto.getSalary());

        Employee saved = repo.save(mapper.toEntity(dto));
        return mapper.toResponseDto(saved);
    }

    // ── READ ONE ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto findById(Long id) {
        Employee e = repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return mapper.toResponseDto(e);
    }

    // ── READ ALL (paginated + filtered) ──────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> findAll(
            String dept, Boolean active, Pageable pageable) {
        return repo.findAllFiltered(dept, active, pageable)
                .map(mapper::toResponseDto);
    }

    // ── FULL UPDATE (PUT) ────────────────────────────────────
    @Override
    public EmployeeResponseDto update(Long id,
                                      @Valid EmployeeRequestDto dto) {

        Employee existing = repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Email uniqueness check — exclude the current record
        repo.findByEmail(dto.getEmail()).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new DuplicateEmailException(dto.getEmail());
            }
        });

        validateSalaryFloor(dto.getDepartment(), dto.getSalary());
        mapper.updateEntityFromDto(dto, existing);
        return mapper.toResponseDto(repo.save(existing));
    }

    // ── PARTIAL UPDATE (PATCH) ───────────────────────────────
    @Override
    public EmployeeResponseDto partialUpdate(Long id,
                                             Map<String, Object> fields) {

        Employee e = repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Only allow safe fields to be patched
        fields.forEach((key, val) -> {
            switch (key) {
                case "salary"     -> e.setSalary(
                        new BigDecimal(val.toString()));
                case "department" -> e.setDepartment(val.toString());
                case "active"     -> e.setActive(
                        Boolean.parseBoolean(val.toString()));
            }
        });

        return mapper.toResponseDto(repo.save(e));
    }

    // ── SOFT DELETE ──────────────────────────────────────────
    @Override
    public void softDelete(Long id) {
        Employee e = repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        e.setActive(false);
        repo.save(e);
        // Row stays in DB — just flagged inactive
    }

    // ── HARD DELETE ──────────────────────────────────────────
    @Override
    public void hardDelete(Long id) {
        Employee e = repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        if (e.getActive()) {
            throw new IllegalStateException(
                    "Cannot hard-delete an active employee. Soft-delete first.");
        }
        repo.delete(e);
    }

    // ── SALARY RANGE ─────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> findBySalaryRange(
            BigDecimal min, BigDecimal max) {
        return repo.findBySalaryRange(min, max)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findFilteredEntities(
            String dept, Boolean active) {
        return repo.findAllFiltered(dept, active,
                Pageable.unpaged()).getContent();
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────
    private void validateSalaryFloor(String dept, BigDecimal salary) {
        BigDecimal floor = dept.equalsIgnoreCase("Intern")
                ? new BigDecimal("15000")
                : new BigDecimal("30000");
        if (salary.compareTo(floor) < 0) {
            throw new IllegalArgumentException(
                    "Minimum salary for " + dept + " is " + floor);
        }
    }
}