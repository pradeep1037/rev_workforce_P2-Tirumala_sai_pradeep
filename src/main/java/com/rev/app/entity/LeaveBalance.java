package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LEAVE_BALANCE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lb_seq")
    @SequenceGenerator(name = "lb_seq", sequenceName = "LEAVE_BALANCE_SEQ", allocationSize = 1)
    @Column(name = "LEAVE_BALANCE_ID")
    private Long leaveBalanceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", unique = true, nullable = false)
    private Employee employee;

    @Column(name = "CASUAL_LEAVE", nullable = false)
    private Integer casualLeave = 12;

    @Column(name = "SICK_LEAVE", nullable = false)
    private Integer sickLeave = 6;

    @Column(name = "PAID_LEAVE", nullable = false)
    private Integer paidLeave = 15;

    // Convenience constructor used when creating default balance
    public LeaveBalance(Employee employee, int casualLeave, int sickLeave, int paidLeave) {
        this.employee = employee;
        this.casualLeave = casualLeave;
        this.sickLeave = sickLeave;
        this.paidLeave = paidLeave;
    }
}
