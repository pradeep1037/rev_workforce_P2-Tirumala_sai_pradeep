package com.rev.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANNOUNCEMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "announce_seq")
    @SequenceGenerator(name = "announce_seq", sequenceName = "ANNOUNCE_SEQ", allocationSize = 1)
    @Column(name = "ANNOUNCEMENT_ID")
    private Long announcementId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "CONTENT", nullable = false, length = 3000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Employee createdBy;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive = true;
}
