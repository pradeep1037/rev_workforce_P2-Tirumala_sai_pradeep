package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ATTENDANCE", uniqueConstraints = @UniqueConstraint(columnNames = { "EMPLOYEE_ID", "DATE_COL" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "att_seq")
    @SequenceGenerator(name = "att_seq", sequenceName = "ATT_SEQ", allocationSize = 1)
    @Column(name = "ATT_ID")
    private Long attId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "DATE_COL", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AttendanceStatus status;

    public enum AttendanceStatus {
        PRESENT, ABSENT, LEAVE, HOLIDAY
    }
}
