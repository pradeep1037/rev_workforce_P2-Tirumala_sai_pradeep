package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long notificationId;
    private Long employeeId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String type;
    private Long refId;
}
