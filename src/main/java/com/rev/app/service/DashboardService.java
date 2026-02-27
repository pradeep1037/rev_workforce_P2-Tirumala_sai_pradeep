package com.rev.app.service;

import com.rev.app.dto.DashboardDTO;
import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveApplication;
import com.rev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final DepartmentRepository departmentRepository;
    private final INotificationService notificationService;

    public DashboardDTO getDashboard(Employee currentUser) {
        DashboardDTO dto = new DashboardDTO();
        dto.setEmployeeName(currentUser.getName());
        dto.setRole(currentUser.getRole().name());

        // Leave balance (for all roles)
        leaveBalanceRepository.findByEmployeeEmployeeId(currentUser.getEmployeeId()).ifPresent(lb -> {
            dto.setCasualLeaveBalance(lb.getCasualLeave());
            dto.setSickLeaveBalance(lb.getSickLeave());
            dto.setPaidLeaveBalance(lb.getPaidLeave());
        });

        // Unread notifications
        dto.setUnreadNotificationCount(notificationService.getUnreadCount(currentUser.getEmployeeId()));

        // My pending leaves
        dto.setPendingLeaveCount((long) leaveApplicationRepository
                .findByEmployeeEmployeeIdAndStatus(currentUser.getEmployeeId(), LeaveApplication.LeaveStatus.PENDING)
                .size());

        if (currentUser.getRole() == Employee.Role.MANAGER || currentUser.getRole() == Employee.Role.ADMIN) {
            dto.setTeamSize((long) employeeRepository.findByManagerEmployeeId(currentUser.getEmployeeId()).size());
            dto.setPendingApprovalCount((long) leaveApplicationRepository
                    .findByManagerEmployeeIdAndStatus(currentUser.getEmployeeId(), LeaveApplication.LeaveStatus.PENDING)
                    .size());
        }

        if (currentUser.getRole() == Employee.Role.ADMIN) {
            dto.setTotalEmployees(employeeRepository.count());
            dto.setActiveEmployees(employeeRepository.findAllActiveEmployees().size() + 0L);
            dto.setTotalDepartments(departmentRepository.count());
            dto.setPendingLeaveApplications((long) leaveApplicationRepository
                    .findAll().stream().filter(l -> l.getStatus() == LeaveApplication.LeaveStatus.PENDING).count());
        }

        return dto;
    }
}
