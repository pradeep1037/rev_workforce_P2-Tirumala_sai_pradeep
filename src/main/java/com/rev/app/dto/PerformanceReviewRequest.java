package com.rev.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PerformanceReviewRequest {

    @NotNull(message = "Review year is required")
    private Integer year;

    private String keyDeliverables;
    private String accomplishments;
    private String areasOfImprovement;

    @Min(value = 1, message = "Self-rating must be between 1 and 5")
    @Max(value = 5, message = "Self-rating must be between 1 and 5")
    private Integer selfRating;
}
