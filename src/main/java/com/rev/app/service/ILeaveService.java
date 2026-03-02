package com.rev.app.service;

import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.Holiday;
import com.rev.app.entity.LeaveBalance;

import java.time.LocalDate;
import java.util.List;

public interface ILeaveService {

    LeaveApplicationDTO applyLeave(Long employeeId, LeaveRequest req);

    LeaveApplicationDTO processLeave(Long leaveId, LeaveActionRequest req, Employee manager);

    void cancelLeave(Long leaveId, Long employeeId);

    List<LeaveApplicationDTO> getMyLeaves(Long employeeId);

    List<LeaveApplicationDTO> getTeamLeaves(Long managerId);

    List<LeaveApplicationDTO> getAllLeaves();

    LeaveBalance getLeaveBalance(Long employeeId);

    List<LeaveBalance> getTeamLeaveBalances(Long managerId);

    List<Holiday> getHolidays();

    Holiday addHoliday(String name, LocalDate date);

    Holiday updateHoliday(Long id, String name, LocalDate date);

    void deleteHoliday(Long id);
}
