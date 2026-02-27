package com.rev.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ManagerFeedbackRequest {

    @NotBlank(message = "Feedback is required")
    private String managerFeedback;

    @NotNull(message = "Manager rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer managerRating;
}
