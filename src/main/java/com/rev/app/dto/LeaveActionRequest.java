package com.rev.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveActionRequest {

    @NotBlank(message = "Action is required (APPROVE or REJECT)")
    private String action; // APPROVE or REJECT

    private String comments;
}
