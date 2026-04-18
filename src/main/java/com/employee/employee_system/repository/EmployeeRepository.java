package com.employee.employee_system.repository;

import com.employee.employee_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Extends JpaRepository → free: save, findById, findAll,
// delete, count, existsById — no SQL needed
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    // Spring reads the method name and generates:
    // SELECT * FROM employee WHERE department = ?
    List<Employee> findByDepartment(String department);

    // SELECT * FROM employee WHERE email = ?  — returns Optional
    // Optional forces the caller to handle "not found" safely
    Optional<Employee> findByEmail(String email);

    // SELECT * FROM employee WHERE active = true
    List<Employee> findByActiveTrue();

    // @Query lets you write JPQL when method names get complex
    // :min and :max are named parameters bound by @Param
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :min AND :max")
    List<Employee> findBySalaryRange(
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max
    );

    // Dynamic filter used by paginated list endpoint
    @Query("SELECT e FROM Employee e WHERE "
            + "(:dept IS NULL OR e.department = :dept) AND "
            + "(:active IS NULL OR e.active = :active)")
    Page<Employee> findAllFiltered(
            @Param("dept") String dept,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
