package com.rev.app.repository;

import com.rev.app.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeEmployeeId(Long employeeId);

    List<LeaveBalance> findByEmployeeManagerEmployeeId(Long managerId);
}
