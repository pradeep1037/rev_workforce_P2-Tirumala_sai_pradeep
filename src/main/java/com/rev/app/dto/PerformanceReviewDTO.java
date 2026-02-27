package com.rev.app.dto;

import lombok.Data;

@Data
public class PerformanceReviewDTO {

    private Long reviewId;
    private Long employeeId;
    private String employeeName;
    private Integer year;
    private String keyDeliverables;
    private String accomplishments;
    private String areasOfImprovement;
    private Integer selfRating;
    private Integer managerRating;
    private String managerFeedback;
    private String status;
}
