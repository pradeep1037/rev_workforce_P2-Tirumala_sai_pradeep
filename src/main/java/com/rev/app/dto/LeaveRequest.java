package com.rev.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequest {

    @NotBlank(message = "Leave type is required")
    private String leaveType; // CASUAL_LEAVE, SICK_LEAVE, PAID_LEAVE

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    private String reason;
}
