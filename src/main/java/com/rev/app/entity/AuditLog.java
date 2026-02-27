package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(name = "audit_seq", sequenceName = "AUDIT_SEQ", allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERFORMED_BY", nullable = false)
    private Employee performedBy;

    @Column(name = "ACTION", nullable = false, length = 300)
    private String action;

    @Column(name = "ACTION_TIME", nullable = false)
    private LocalDateTime actionTime = LocalDateTime.now();

    @Column(name = "ENTITY_TYPE", length = 50)
    private String entityType;

    @Column(name = "ENTITY_ID")
    private Long entityId;
}
