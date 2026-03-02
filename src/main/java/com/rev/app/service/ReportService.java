package com.rev.app.service;

import com.rev.app.dto.EmployeeReportDTO;
import com.rev.app.dto.LeaveReportDTO;
import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveApplication;
import com.rev.app.entity.LeaveBalance;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.LeaveApplicationRepository;
import com.rev.app.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;

    @Override
    public List<EmployeeReportDTO> generateEmployeeReport(Long departmentId, String status) {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .filter(e -> departmentId == null
                        || (e.getDepartment() != null && e.getDepartment().getDeptId().equals(departmentId)))
                .filter(e -> status == null || status.isBlank() || e.getStatus().name().equalsIgnoreCase(status))
                .map(e -> new EmployeeReportDTO(
                        e.getEmployeeId(),
                        e.getName(),
                        e.getEmail(),
                        e.getDepartment() != null ? e.getDepartment().getDeptName() : "N/A",
                        e.getDesignation() != null ? e.getDesignation().getDesigName() : "N/A",
                        e.getManager() != null ? e.getManager().getName() : "N/A",
                        e.getJoiningDate(),
                        e.getStatus().name(),
                        e.getRole().name()))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveReportDTO> generateLeaveReport(Long departmentId) {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .filter(e -> departmentId == null
                        || (e.getDepartment() != null && e.getDepartment().getDeptId().equals(departmentId)))
                .map(e -> {
                    LeaveBalance balance = leaveBalanceRepository.findByEmployeeEmployeeId(e.getEmployeeId())
                            .orElse(null);
                    List<LeaveApplication> applications = leaveApplicationRepository
                            .findByEmployeeEmployeeId(e.getEmployeeId());

                    long taken = applications.stream()
                            .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.APPROVED)
                            .mapToLong(a -> a.getToDate().toEpochDay() - a.getFromDate().toEpochDay() + 1)
                            .sum();

                    long pending = applications.stream()
                            .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.PENDING)
                            .mapToLong(a -> a.getToDate().toEpochDay() - a.getFromDate().toEpochDay() + 1)
                            .sum();

                    return new LeaveReportDTO(
                            e.getEmployeeId(),
                            e.getName(),
                            e.getDepartment() != null ? e.getDepartment().getDeptName() : "N/A",
                            balance != null ? balance.getCasualLeave() : 0,
                            balance != null ? balance.getSickLeave() : 0,
                            balance != null ? balance.getPaidLeave() : 0,
                            taken,
                            pending);
                }).collect(Collectors.toList());
    }
}
