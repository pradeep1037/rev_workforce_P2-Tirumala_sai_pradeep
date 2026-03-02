package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalDTO {
    private Long goalId;
    private Long employeeId;
    private String employeeName;
    private String goalDescription;
    private LocalDate deadline;
    private String priority;
    private String status;
    private Integer progressPercent;
    private String managerComments;
    private Long reviewId;
}
