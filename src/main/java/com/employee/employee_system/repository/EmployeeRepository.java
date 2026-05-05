package com.employee.employee_system.repository;

import com.employee.employee_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

// Extends JpaRepository → free: save, findById, findAll,
// delete, count, existsById — no SQL needed
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    // Dynamic filter — all params are optional (null = ignored)
    @Query("SELECT e FROM Employee e WHERE "
            + "(:dept IS NULL OR e.department = :dept) AND "
            + "(:active IS NULL OR e.active = :active) AND "
            + "(:minSalary IS NULL OR e.salary >= :minSalary) AND "
            + "(:maxSalary IS NULL OR e.salary <= :maxSalary)")
    Page<Employee> findAllFiltered(
            @Param("dept")      String     dept,
            @Param("active")    Boolean    active,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            Pageable pageable
    );
}
