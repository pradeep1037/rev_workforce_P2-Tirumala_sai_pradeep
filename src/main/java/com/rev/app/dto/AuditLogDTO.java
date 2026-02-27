package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {

    private Long logId;
    private Long performedById;
    private String performedByName;
    private String performedByEmail;
    private String action;
    private LocalDateTime actionTime;
    private String entityType;
    private Long entityId;
}
