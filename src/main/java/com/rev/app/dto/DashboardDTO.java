package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // Common
    private String employeeName;
    private String role;

    // Employee-facing
    private Integer casualLeaveBalance;
    private Integer sickLeaveBalance;
    private Integer paidLeaveBalance;
    private Long pendingLeaveCount;
    private Long unreadNotificationCount;

    // Manager-facing
    private Long teamSize;
    private Long pendingApprovalCount;

    // Admin-facing
    private Long totalEmployees;
    private Long activeEmployees;
    private Long totalLeaveApplicationsThisMonth;
    private Long pendingLeaveApplications;
    private Long totalDepartments;
}
