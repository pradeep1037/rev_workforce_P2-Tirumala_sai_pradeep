package com.rev.app.service;

import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.*;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.LeaveApplicationMapper;
import com.rev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService implements ILeaveService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final HolidayRepository holidayRepository;
    private final EmployeeRepository employeeRepository;
    private final INotificationService notificationService;
    private final IAuditLogService auditLogService;
    private final LeaveApplicationMapper leaveApplicationMapper;

    @Override
    @Transactional
    public LeaveApplicationDTO applyLeave(Long employeeId, LeaveRequest req) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new BadRequestException("From date cannot be after to date");
        }
        if (req.getFromDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply leave for past dates");
        }

        int days = (int) (req.getToDate().toEpochDay() - req.getFromDate().toEpochDay() + 1);
        LeaveApplication.LeaveType leaveType = LeaveApplication.LeaveType.valueOf(req.getLeaveType());

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new BadRequestException("Leave balance not found"));
        validateAndDeductBalance(balance, leaveType, days);

        LeaveApplication la = new LeaveApplication();
        la.setEmployee(employee);
        la.setManager(employee.getManager());
        la.setLeaveType(leaveType);
        la.setFromDate(req.getFromDate());
        la.setToDate(req.getToDate());
        la.setReason(req.getReason());
        la.setStatus(LeaveApplication.LeaveStatus.PENDING);
        la.setAppliedOn(LocalDateTime.now());

        LeaveApplication saved = leaveApplicationRepository.save(la);

        if (employee.getManager() != null) {
            notificationService.send(
                    employee.getManager(),
                    employee.getName() + " has applied for " + leaveType.name().replace("_", " ") + " from "
                            + req.getFromDate() + " to " + req.getToDate(),
                    Notification.NotificationType.LEAVE_APPLIED,
                    saved.getLeaveId());
        }

        return leaveApplicationMapper.toDto(saved);
    }

    private void validateAndDeductBalance(LeaveBalance balance, LeaveApplication.LeaveType type, int days) {
        switch (type) {
            case CASUAL_LEAVE -> {
                if (balance.getCasualLeave() < days)
                    throw new BadRequestException("Insufficient casual leave balance");
                balance.setCasualLeave(balance.getCasualLeave() - days);
            }
            case SICK_LEAVE -> {
                if (balance.getSickLeave() < days)
                    throw new BadRequestException("Insufficient sick leave balance");
                balance.setSickLeave(balance.getSickLeave() - days);
            }
            case PAID_LEAVE -> {
                if (balance.getPaidLeave() < days)
                    throw new BadRequestException("Insufficient paid leave balance");
                balance.setPaidLeave(balance.getPaidLeave() - days);
            }
        }
        leaveBalanceRepository.save(balance);
    }

    private void restoreBalance(LeaveBalance balance, LeaveApplication.LeaveType type, int days) {
        switch (type) {
            case CASUAL_LEAVE -> balance.setCasualLeave(balance.getCasualLeave() + days);
            case SICK_LEAVE -> balance.setSickLeave(balance.getSickLeave() + days);
            case PAID_LEAVE -> balance.setPaidLeave(balance.getPaidLeave() + days);
        }
        leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveApplicationDTO processLeave(Long leaveId, LeaveActionRequest req, Employee manager) {
        LeaveApplication la = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application", "id", leaveId));

        if (la.getStatus() != LeaveApplication.LeaveStatus.PENDING) {
            throw new BadRequestException("Leave application is already " + la.getStatus());
        }

        // Authorization check: Must be the applicant's manager OR an ADMIN
        boolean isAdmin = "ADMIN".equals(manager.getRole());
        boolean isDirectManager = la.getManager() != null
                && la.getManager().getEmployeeId().equals(manager.getEmployeeId());

        if (!isAdmin && !isDirectManager) {
            throw new BadRequestException("You are not authorized to process this leave application");
        }

        boolean approve = "APPROVE".equalsIgnoreCase(req.getAction());
        boolean isSelfApproval = la.getEmployee().getEmployeeId().equals(manager.getEmployeeId());

        if (!approve && (req.getComments() == null || req.getComments().isBlank())) {
            throw new BadRequestException("Comments are required when rejecting a leave request");
        }

        if (approve && isSelfApproval && (req.getComments() == null || req.getComments().isBlank())) {
            throw new BadRequestException("A proper reason (comments) is required for self-approval");
        }

        la.setStatus(approve ? LeaveApplication.LeaveStatus.APPROVED : LeaveApplication.LeaveStatus.REJECTED);
        la.setManagerComments(req.getComments());

        if (!approve) {
            int days = (int) (la.getToDate().toEpochDay() - la.getFromDate().toEpochDay() + 1);
            leaveBalanceRepository.findByEmployeeEmployeeId(la.getEmployee().getEmployeeId())
                    .ifPresent(b -> restoreBalance(b, la.getLeaveType(), days));
        }

        leaveApplicationRepository.save(la);

        String msg = approve
                ? "Your leave request from " + la.getFromDate() + " to " + la.getToDate() + " has been APPROVED."
                : "Your leave request from " + la.getFromDate() + " to " + la.getToDate()
                        + " has been REJECTED. Reason: " + req.getComments();
        notificationService.send(la.getEmployee(), msg,
                approve ? Notification.NotificationType.LEAVE_APPROVED : Notification.NotificationType.LEAVE_REJECTED,
                leaveId);

        auditLogService.log(manager,
                (approve ? "Approved" : "Rejected") + " leave #" + leaveId + " for " + la.getEmployee().getName(),
                "LeaveApplication", leaveId);
        return leaveApplicationMapper.toDto(la);
    }

    @Override
    @Transactional
    public void cancelLeave(Long leaveId, Long employeeId) {
        LeaveApplication la = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application", "id", leaveId));

        if (!la.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new BadRequestException("You can only cancel your own leave applications");
        }
        if (la.getStatus() != LeaveApplication.LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave applications can be cancelled");
        }

        int days = (int) (la.getToDate().toEpochDay() - la.getFromDate().toEpochDay() + 1);
        leaveBalanceRepository.findByEmployeeEmployeeId(employeeId)
                .ifPresent(b -> restoreBalance(b, la.getLeaveType(), days));

        la.setStatus(LeaveApplication.LeaveStatus.CANCELLED);
        leaveApplicationRepository.save(la);
    }

    @Override
    public List<LeaveApplicationDTO> getMyLeaves(Long employeeId) {
        return leaveApplicationRepository.findByEmployeeEmployeeId(employeeId)
                .stream().map(leaveApplicationMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LeaveApplicationDTO> getTeamLeaves(Long managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", managerId));

        if ("ADMIN".equals(manager.getRole())) {
            // Admin sees all leaves in the system to allow override/company-wide approval
            return leaveApplicationRepository.findAll()
                    .stream().map(leaveApplicationMapper::toDto).collect(Collectors.toList());
        } else {
            return leaveApplicationRepository.findTeamLeaveApplications(managerId)
                    .stream().map(leaveApplicationMapper::toDto).collect(Collectors.toList());
        }
    }

    @Override
    public List<LeaveApplicationDTO> getAllLeaves() {
        return leaveApplicationRepository.findAll()
                .stream().map(leaveApplicationMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LeaveBalance getLeaveBalance(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance", "employeeId", employeeId));
    }

    @Override
    public List<LeaveBalance> getTeamLeaveBalances(Long managerId) {
        return leaveBalanceRepository.findByEmployeeManagerEmployeeId(managerId);
    }

    @Override
    public List<Holiday> getHolidays() {
        return holidayRepository.findAll();
    }

    @Override
    @Transactional
    public Holiday addHoliday(String name, LocalDate date) {
        if (holidayRepository.existsByHolidayDate(date)) {
            throw new BadRequestException("Holiday already exists for date: " + date);
        }
        Holiday h = new Holiday(null, name, date);
        return holidayRepository.save(h);
    }

    @Override
    @Transactional
    public Holiday updateHoliday(Long id, String name, LocalDate date) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
        if (!h.getHolidayDate().equals(date) && holidayRepository.existsByHolidayDate(date)) {
            throw new BadRequestException("Holiday already exists for date: " + date);
        }
        h.setHolidayName(name);
        h.setHolidayDate(date);
        return holidayRepository.save(h);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id))
            throw new ResourceNotFoundException("Holiday", "id", id);
        holidayRepository.deleteById(id);
    }
}
