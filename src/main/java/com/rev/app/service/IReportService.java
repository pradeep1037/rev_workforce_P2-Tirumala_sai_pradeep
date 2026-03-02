package com.rev.app.service;

import com.rev.app.dto.EmployeeReportDTO;
import com.rev.app.dto.LeaveReportDTO;

import java.util.List;

public interface IReportService {
    List<EmployeeReportDTO> generateEmployeeReport(Long departmentId, String status);

    List<LeaveReportDTO> generateLeaveReport(Long departmentId);
}
