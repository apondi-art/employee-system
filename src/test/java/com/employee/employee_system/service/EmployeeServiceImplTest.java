package com.employee.employee_system.service;

import com.employee.employee_system.Dto.EmployeeRequestDto;
import com.employee.employee_system.Dto.EmployeeResponseDto;
import com.employee.employee_system.Exception.DuplicateEmailException;
import com.employee.employee_system.Exception.EmployeeNotFoundException;
import com.employee.employee_system.Mapper.EmployeeMapper;
import com.employee.employee_system.entity.Employee;
import com.employee.employee_system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repo;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeServiceImpl service;

    private EmployeeRequestDto requestDto;
    private Employee employee;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = EmployeeRequestDto.builder()
                .firstName("John")
                .lastName("Teri")
                .email("john@example.com")
                .department("Engineering")
                .salary(new BigDecimal("55000"))
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Teri")
                .email("john@example.com")
                .department("Engineering")
                .salary(new BigDecimal("55000"))
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        responseDto = EmployeeResponseDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Teri")
                .email("john@example.com")
                .department("Engineering")
                .salary(new BigDecimal("55000"))
                .dateOfJoining(LocalDate.of(2024, 1, 15))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void create_happyPath_returnsResponseDto() {
        when(repo.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(employee);
        when(repo.save(employee)).thenReturn(employee);
        when(mapper.toResponseDto(employee)).thenReturn(responseDto);

        EmployeeResponseDto result = service.create(requestDto);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(repo).save(employee);
    }

    @Test
    void create_duplicateEmail_throwsDuplicateEmailException() {
        when(repo.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> service.create(requestDto))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void create_salaryBelowFloor_throwsIllegalArgumentException() {
        requestDto = EmployeeRequestDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .department("Engineering")
                .salary(new BigDecimal("10000"))   // below 30 000 floor
                .dateOfJoining(LocalDate.now())
                .active(true)
                .build();

        when(repo.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum salary");

        verify(repo, never()).save(any());
    }

    @Test
    void create_internSalaryAtFloor_succeeds() {
        requestDto = EmployeeRequestDto.builder()
                .firstName("Sam")
                .lastName("Lee")
                .email("sam@example.com")
                .department("Intern")
                .salary(new BigDecimal("15000"))   // exactly the intern floor
                .dateOfJoining(LocalDate.now())
                .active(true)
                .build();

        Employee internEntity = Employee.builder().id(2L).build();

        when(repo.findByEmail(any())).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(internEntity);
        when(repo.save(internEntity)).thenReturn(internEntity);
        when(mapper.toResponseDto(internEntity)).thenReturn(responseDto);

        assertThatNoException().isThrownBy(() -> service.create(requestDto));
    }

    // ── FIND BY ID ───────────────────────────────────────────────────────────

    @Test
    void findById_found_returnsDto() {
        when(repo.findById(1L)).thenReturn(Optional.of(employee));
        when(mapper.toResponseDto(employee)).thenReturn(responseDto);

        EmployeeResponseDto result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throwsEmployeeNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void update_happyPath_returnsUpdatedDto() {
        when(repo.findById(1L)).thenReturn(Optional.of(employee));
        when(repo.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(employee));
        when(repo.save(employee)).thenReturn(employee);
        when(mapper.toResponseDto(employee)).thenReturn(responseDto);

        EmployeeResponseDto result = service.update(1L, requestDto);

        assertThat(result).isNotNull();
        verify(mapper).updateEntityFromDto(requestDto, employee);
    }

    @Test
    void update_notFound_throwsEmployeeNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, requestDto))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void update_emailTakenByOther_throwsDuplicateEmailException() {
        Employee other = Employee.builder().id(2L).email("john@example.com").build();

        when(repo.findById(1L)).thenReturn(Optional.of(employee));
        when(repo.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(1L, requestDto))
                .isInstanceOf(DuplicateEmailException.class);
    }

    // ── SOFT DELETE ──────────────────────────────────────────────────────────

    @Test
    void softDelete_setsActiveFalse() {
        when(repo.findById(1L)).thenReturn(Optional.of(employee));

        service.softDelete(1L);

        assertThat(employee.getActive()).isFalse();
        verify(repo).save(employee);
    }

    @Test
    void softDelete_notFound_throwsEmployeeNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDelete(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    // ── HARD DELETE ──────────────────────────────────────────────────────────

    @Test
    void hardDelete_inactiveEmployee_deletesRow() {
        employee.setActive(false);
        when(repo.findById(1L)).thenReturn(Optional.of(employee));

        service.hardDelete(1L);

        verify(repo).delete(employee);
    }

    @Test
    void hardDelete_activeEmployee_throwsIllegalStateException() {
        when(repo.findById(1L)).thenReturn(Optional.of(employee)); // active = true

        assertThatThrownBy(() -> service.hardDelete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot hard-delete");

        verify(repo, never()).delete(any());
    }

    @Test
    void hardDelete_notFound_throwsEmployeeNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hardDelete(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
