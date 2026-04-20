package com.employee.employee_system.controller;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanDatabase() {
        employeeRepository.deleteAll();
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private EmployeeRequestDto validRequest() {
        return EmployeeRequestDto.builder()
                .firstName("John")
                .lastName("Teri")
                .email("john@example.com")
                .department("Engineering")
                .salary(new BigDecimal("55000"))
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
    }

    /** POST a valid employee and return the created ID. */
    private Long createEmployee(EmployeeRequestDto dto) throws Exception {
        String body = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    // ── POST /api/v1/employees ────────────────────────────────────────────────

    @Test
    void createEmployee_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.department").value("Engineering"));
    }

    @Test
    void createEmployee_missingFirstName_returns400WithFieldError() throws Exception {
        EmployeeRequestDto bad = EmployeeRequestDto.builder()
                .lastName("Teri")
                .email("john@example.com")
                .department("Engineering")
                .salary(new BigDecimal("55000"))
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    void createEmployee_invalidEmail_returns400() throws Exception {
        EmployeeRequestDto bad = validRequest();
        bad.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createEmployee_duplicateEmail_returns409() throws Exception {
        createEmployee(validRequest());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void createEmployee_salaryBelowFloor_returns400() throws Exception {
        EmployeeRequestDto bad = validRequest();
        bad.setSalary(new BigDecimal("5000"));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Minimum salary")));
    }

    // ── GET /api/v1/employees/{id} ────────────────────────────────────────────

    @Test
    void getById_exists_returns200() throws Exception {
        Long id = createEmployee(validRequest());

        mockMvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/employees ─────────────────────────────────────────────────

    @Test
    void getAll_returnsPagedResults() throws Exception {
        createEmployee(validRequest());

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAll_filterByDepartment_returnsMatchingEmployees() throws Exception {
        createEmployee(validRequest());

        EmployeeRequestDto other = validRequest();
        other.setEmail("other@example.com");
        other.setDepartment("HR");
        other.setSalary(new BigDecimal("35000"));
        createEmployee(other);

        mockMvc.perform(get("/api/v1/employees")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].department").value("Engineering"));
    }

    // ── PUT /api/v1/employees/{id} ────────────────────────────────────────────

    @Test
    void update_validRequest_returns200() throws Exception {
        Long id = createEmployee(validRequest());

        EmployeeRequestDto updated = validRequest();
        updated.setFirstName("Jane");
        updated.setSalary(new BigDecimal("60000"));

        mockMvc.perform(put("/api/v1/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.salary").value(60000));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/employees/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/employees/{id} (soft) ─────────────────────────────────

    @Test
    void softDelete_exists_returns204AndSetsInactive() throws Exception {
        Long id = createEmployee(validRequest());

        mockMvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isNoContent());

        // employee still exists but active = false
        mockMvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void softDelete_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/employees/{id}/hard ───────────────────────────────────

    @Test
    void hardDelete_inactiveEmployee_returns204() throws Exception {
        Long id = createEmployee(validRequest());
        mockMvc.perform(delete("/api/v1/employees/{id}", id));   // soft-delete first

        mockMvc.perform(delete("/api/v1/employees/{id}/hard", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void hardDelete_activeEmployee_returns400() throws Exception {
        Long id = createEmployee(validRequest());   // still active

        mockMvc.perform(delete("/api/v1/employees/{id}/hard", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot hard-delete")));
    }

    // ── GET /api/v1/employees/salary-range ───────────────────────────────────

    @Test
    void salaryRange_returnsMatchingEmployees() throws Exception {
        createEmployee(validRequest());   // salary 55 000

        mockMvc.perform(get("/api/v1/employees/salary-range")
                        .param("min", "40000")
                        .param("max", "70000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void salaryRange_noMatch_returnsEmptyList() throws Exception {
        createEmployee(validRequest());   // salary 55 000

        mockMvc.perform(get("/api/v1/employees/salary-range")
                        .param("min", "80000")
                        .param("max", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
