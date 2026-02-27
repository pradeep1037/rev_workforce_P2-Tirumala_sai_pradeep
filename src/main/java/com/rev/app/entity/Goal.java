package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "GOAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goal_seq")
    @SequenceGenerator(name = "goal_seq", sequenceName = "GOAL_SEQ", allocationSize = 1)
    @Column(name = "GOAL_ID")
    private Long goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "GOAL_DESCRIPTION", nullable = false, length = 1000)
    private String goalDescription;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY", nullable = false, length = 10)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Column(name = "PROGRESS_PERCENT")
    private Integer progressPercent = 0;

    @Column(name = "MANAGER_COMMENTS", length = 1000)
    private String managerComments;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum GoalStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
