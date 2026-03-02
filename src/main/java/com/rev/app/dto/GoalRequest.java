package com.rev.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalRequest {

    @NotBlank(message = "Goal description is required")
    private String goalDescription;

    private LocalDate deadline;

    @NotBlank(message = "Priority is required")
    private String priority; // HIGH, MEDIUM, LOW

    private Long employeeId;
    private Long reviewId;
}
