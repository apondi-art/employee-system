package com.employee.employee_system.controller;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.Dto.ImportResultDto;
import com.employee.employee_system.FilePdf.ExcelService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ExcelService excelService;
    private final PdfService pdfService;

    // POST /api/v1/employees
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(
            @Valid @org.springframework.web.bind.annotation.RequestBody EmployeeRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.create(dto));
    }

    // GET /api/v1/employees?page=0&size=10&sort=id&department=IT&active=true
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> getAll(
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "10") int     size,
            @RequestParam(defaultValue = "id") String  sort,
            @RequestParam(required = false)    String  department,
            @RequestParam(required = false)    Boolean active) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(
                employeeService.findAll(department, active, pageable));
    }

    // GET /api/v1/employees/99
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    // PUT /api/v1/employees/99 — full replace
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    // PATCH /api/v1/employees/99 — partial update
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> partialUpdate(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(
                employeeService.partialUpdate(id, fields));
    }

    // DELETE /api/v1/employees/99 — soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/employees/99/hard — permanent remove
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        employeeService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/employees/salary-range?min=30000&max=90000
    @GetMapping("/salary-range")
    public ResponseEntity<List<EmployeeResponseDto>> bySalary(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(
                employeeService.findBySalaryRange(min, max));
    }

    // POST /api/v1/employees/import
    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importExcel(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                excelService.importEmployees(file));
    }

    // GET /api/v1/employees/export/excel
    @GetMapping("/export/excel")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String  department,
            @RequestParam(required = false) Boolean active)
            throws IOException {
        List<Employee> list =
                employeeService.findFilteredEntities(department, active);
        excelService.exportToExcel(response, list);
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