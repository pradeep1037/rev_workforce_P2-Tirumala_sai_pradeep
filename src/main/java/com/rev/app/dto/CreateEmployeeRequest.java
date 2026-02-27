package com.rev.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // EMPLOYEE, MANAGER, ADMIN

    private Long departmentId;
    private Long designationId;
    private Long managerId;
    private String phone;
    private String address;
    private String emergencyContact;
    private LocalDate joiningDate;
    private Double salary;
}
