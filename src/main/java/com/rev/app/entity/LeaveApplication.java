package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LEAVE_APPLICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="leave_id")
    private Long leaveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "LEAVE_TYPE", nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(name = "FROM_DATE", nullable = false)
    private LocalDate fromDate;

    @Column(name = "TO_DATE", nullable = false)
    private LocalDate toDate;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "MANAGER_COMMENTS", length = 500)
    private String managerComments;

    @Column(name = "APPLIED_ON")
    private LocalDateTime appliedOn = LocalDateTime.now();

    public enum LeaveType {
        CASUAL_LEAVE, SICK_LEAVE, PAID_LEAVE
    }

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
