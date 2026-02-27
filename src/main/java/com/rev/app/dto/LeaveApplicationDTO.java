package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveApplicationDTO {

    private Long leaveId;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long managerId;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;
    private String managerComments;
    private LocalDateTime appliedOn;
    private Integer days;
}
