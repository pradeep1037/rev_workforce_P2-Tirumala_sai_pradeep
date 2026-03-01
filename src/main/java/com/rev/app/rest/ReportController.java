package com.rev.app.rest;

import com.rev.app.dto.EmployeeReportDTO;
import com.rev.app.dto.LeaveReportDTO;
import com.rev.app.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeReportDTO>> getEmployeeReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(reportService.generateEmployeeReport(departmentId, status));
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveReportDTO>> getLeaveReport(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(reportService.generateLeaveReport(departmentId));
    }
}
