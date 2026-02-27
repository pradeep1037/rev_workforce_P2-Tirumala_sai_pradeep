package com.rev.app.service;

import com.rev.app.dto.CreateEmployeeRequest;
import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.UpdateProfileRequest;
import com.rev.app.entity.Employee;

import java.util.List;

public interface IEmployeeService {

    List<EmployeeDTO> getAllEmployees();

    List<EmployeeDTO> searchEmployees(String keyword);

    List<EmployeeDTO> getActiveEmployees();

    EmployeeDTO getEmployeeById(Long id);

    List<EmployeeDTO> getDirectReportees(Long managerId);

    EmployeeDTO createEmployee(CreateEmployeeRequest req, Employee createdBy);

    EmployeeDTO updateEmployee(Long id, CreateEmployeeRequest req, Employee updatedBy);

    void updateProfile(Long employeeId, UpdateProfileRequest req, Employee currentUser);

    void deactivateEmployee(Long id, Employee deactivatedBy);

    void reactivateEmployee(Long id, Employee reactivatedBy);

    void changeManager(Long employeeId, Long managerId, Employee changedBy);
}
