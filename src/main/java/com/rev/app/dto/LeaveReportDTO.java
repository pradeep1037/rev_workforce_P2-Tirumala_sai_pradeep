package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveReportDTO {
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private Integer casualLeaveBalance;
    private Integer sickLeaveBalance;
    private Integer paidLeaveBalance;
    private Long totalLeavesTaken;
    private Long pendingLeaves;
}
