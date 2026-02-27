package com.rev.app.repository;

import com.rev.app.entity.LeaveApplication;
import com.rev.app.entity.LeaveApplication.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeEmployeeId(Long employeeId);

    List<LeaveApplication> findByManagerEmployeeId(Long managerId);

    List<LeaveApplication> findByManagerEmployeeIdAndStatus(Long managerId, LeaveStatus status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.employeeId = :managerId ORDER BY la.appliedOn DESC")
    List<LeaveApplication> findTeamLeaveApplications(@Param("managerId") Long managerId);

    List<LeaveApplication> findByEmployeeEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
}
