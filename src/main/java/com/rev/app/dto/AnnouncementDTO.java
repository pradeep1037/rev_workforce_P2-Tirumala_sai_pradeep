package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {

    private Long announcementId;
    private String title;
    private String content;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
