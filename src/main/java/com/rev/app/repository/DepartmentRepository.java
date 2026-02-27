package com.rev.app.repository;

import com.rev.app.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDeptNameIgnoreCase(String deptName);

    boolean existsByDeptNameIgnoreCase(String deptName);
}
