package com.rev.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Please provide a valid @gmail.com email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // EMPLOYEE, MANAGER, ADMIN

    private Long departmentId;
    private Long designationId;
    private Long managerId;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6789]\\d{9}$", message = "Please provide a valid Indian phone number")
    private String phone;
    private String address;
    private String emergencyContact;
    private LocalDate joiningDate;
    private Double salary;
}
