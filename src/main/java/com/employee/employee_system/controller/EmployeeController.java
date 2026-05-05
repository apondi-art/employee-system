package com.employee.employee_system.controller;

import com.employee.employee_system.Dto.ApiResponse;
import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.FilePdf.PdfService;
import com.employee.employee_system.entity.Employee;
import com.employee.employee_system.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PdfService pdfService;

    // POST /api/v1/employees
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> create(
            @Valid @org.springframework.web.bind.annotation.RequestBody EmployeeRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", employeeService.create(dto)));
    }

    // GET /api/v1/employees?page=0&size=10&sort=id&department=IT&active=true&minSalary=30000&maxSalary=90000
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponseDto>>> getAll(
            @RequestParam(defaultValue = "0")  int        page,
            @RequestParam(defaultValue = "10") int        size,
            @RequestParam(defaultValue = "id") String     sort,
            @RequestParam(required = false)    String     department,
            @RequestParam(required = false)    Boolean    active,
            @RequestParam(required = false)    BigDecimal minSalary,
            @RequestParam(required = false)    BigDecimal maxSalary) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.findAll(department, active, minSalary, maxSalary, pageable)));
    }

    // GET /api/v1/employees/99
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    // PUT /api/v1/employees/99 — full replace
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> update(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee updated successfully", employeeService.update(id, dto)));
    }

    // PATCH /api/v1/employees/99 — partial update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> partialUpdate(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee updated successfully", employeeService.partialUpdate(id, fields)));
    }

    // DELETE /api/v1/employees/99 — soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated successfully", null));
    }

    // DELETE /api/v1/employees/99/hard — permanent remove
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        employeeService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee permanently deleted", null));
    }

    // GET /api/v1/employees/export/pdf
    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse response)
            throws IOException {
        List<Employee> all =
                employeeService.findFilteredEntities(null, null);
        pdfService.generateReport(response, all);
    }
}