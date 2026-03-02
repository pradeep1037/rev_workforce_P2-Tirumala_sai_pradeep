package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReportDTO {
    private Long employeeId;
    private String name;
    private String email;
    private String departmentName;
    private String designationName;
    private String managerName;
    private LocalDate joiningDate;
    private String status;
    private String role;
}
