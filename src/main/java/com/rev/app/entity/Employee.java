package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "EMPLOYEE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", sequenceName = "EMP_SEQ", allocationSize = 1)
    @Column(name = "EMPLOYEE_ID")
    private Long employeeId;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    /**
     * Self-referencing FK: manager_id -> EMPLOYEE.employee_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID")
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIG_ID")
    private Designation designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "ADDRESS", length = 300)
    private String address;

    @Column(name = "EMERGENCY_CONTACT", length = 200)
    private String emergencyContact;

    @Column(name = "JOINING_DATE")
    private LocalDate joiningDate;

    @Column(name = "SALARY")
    private Double salary;

    @Column(name = "SECURITY_QUESTION", length = 200)
    private String securityQuestion;

    @Column(name = "SECURITY_ANSWER", length = 200)
    private String securityAnswer;

    public enum Role {
        EMPLOYEE, MANAGER, ADMIN
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE
    }
}
