package com.rev.app.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long employeeId;
    private String name;
    private String email;
    private String role;
    private String status;
    private String phone;
    private String address;
    private String emergencyContact;
    private LocalDate joiningDate;
    private Double salary;

    // Nested fields
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private Long managerId;
    private String managerName;
    private String managerEmail;
}
