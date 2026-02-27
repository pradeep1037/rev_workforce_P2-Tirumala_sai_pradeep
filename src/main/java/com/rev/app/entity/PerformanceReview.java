package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PERFORMANCE_REVIEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pr_seq")
    @SequenceGenerator(name = "pr_seq", sequenceName = "PR_SEQ", allocationSize = 1)
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "REVIEW_YEAR", nullable = false)
    private Integer year;

    @Column(name = "KEY_DELIVERABLES", length = 2000)
    private String keyDeliverables;

    @Column(name = "ACCOMPLISHMENTS", length = 2000)
    private String accomplishments;

    @Column(name = "AREAS_OF_IMPROVEMENT", length = 2000)
    private String areasOfImprovement;

    @Column(name = "SELF_RATING")
    private Integer selfRating;

    @Column(name = "MANAGER_RATING")
    private Integer managerRating;

    @Column(name = "MANAGER_FEEDBACK", length = 2000)
    private String managerFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20)
    private ReviewStatus status = ReviewStatus.DRAFT;

    public enum ReviewStatus {
        DRAFT, SUBMITTED, REVIEWED
    }
}
