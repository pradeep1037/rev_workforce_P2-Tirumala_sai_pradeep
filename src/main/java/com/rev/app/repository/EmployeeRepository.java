package com.rev.app.repository;

import com.rev.app.entity.Employee;
import com.rev.app.entity.Employee.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByManagerEmployeeId(Long managerId);

    List<Employee> findByRole(Role role);

    List<Employee> findByDepartmentDeptId(Long deptId);

    @Query("SELECT e FROM Employee e WHERE " +
            "CAST(e.employeeId AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.department.deptName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.designation.desigName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);

    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' ORDER BY e.name")
    List<Employee> findAllActiveEmployees();
}
