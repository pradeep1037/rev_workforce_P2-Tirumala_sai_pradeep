package com.rev.app.repository;

import com.rev.app.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByEmployeeEmployeeId(Long employeeId);

    List<Goal> findByEmployeeManagerEmployeeId(Long managerId);

    List<Goal> findByEmployeeEmployeeIdAndStatus(Long employeeId, Goal.GoalStatus status);
}
