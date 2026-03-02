package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "MESSAGE", nullable = false, length = 500)
    private String message;

    @Column(name = "IS_READ", nullable = false)
    private Boolean isRead = false;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", length = 30)
    private NotificationType type;

    /** Reference ID (e.g. leaveId or reviewId) for context-linking */
    @Column(name = "REF_ID")
    private Long refId;

    public enum NotificationType {
        LEAVE_APPLIED, LEAVE_APPROVED, LEAVE_REJECTED,
        REVIEW_SUBMITTED, REVIEW_FEEDBACK, GOAL_ASSIGNED, GENERAL
    }
}
