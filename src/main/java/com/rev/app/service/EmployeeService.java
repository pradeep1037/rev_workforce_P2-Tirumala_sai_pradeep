package com.rev.app.service;

import com.rev.app.dto.CreateEmployeeRequest;
import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.UpdateProfileRequest;
import com.rev.app.entity.*;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EmployeeMapper;
import com.rev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAuditLogService auditLogService;
    private final EmployeeMapper employeeMapper;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> searchEmployees(String keyword) {
        return employeeRepository.searchEmployees(keyword).stream()
                .map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findAllActiveEmployees().stream()
                .map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toDto(e);
    }

    @Override
    public List<EmployeeDTO> getDirectReportees(Long managerId) {
        return employeeRepository.findByManagerEmployeeId(managerId).stream()
                .map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest req, Employee createdBy) {
        if (employeeRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }

        Employee e = new Employee();
        e.setName(req.getName());
        e.setEmail(req.getEmail());
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setRole(Employee.Role.valueOf(req.getRole()));
        e.setStatus(Employee.EmployeeStatus.ACTIVE);
        e.setPhone(req.getPhone());
        e.setAddress(req.getAddress());
        e.setEmergencyContact(req.getEmergencyContact());
        e.setJoiningDate(req.getJoiningDate());
        e.setSalary(req.getSalary());

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.getDepartmentId()));
            e.setDepartment(dept);
        }
        if (req.getDesignationId() != null) {
            Designation desig = designationRepository.findById(req.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", req.getDesignationId()));
            e.setDesignation(desig);
        }
        if (req.getManagerId() != null) {
            Employee manager = employeeRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", req.getManagerId()));
            e.setManager(manager);
        }

        Employee saved = employeeRepository.save(e);
        leaveBalanceRepository.save(new LeaveBalance(saved, 12, 6, 15));

        auditLogService.log(createdBy, "Created employee: " + saved.getName() + " (" + saved.getEmail() + ")",
                "Employee", saved.getEmployeeId());
        return employeeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, CreateEmployeeRequest req, Employee updatedBy) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        e.setName(req.getName());
        e.setPhone(req.getPhone());
        e.setAddress(req.getAddress());
        e.setEmergencyContact(req.getEmergencyContact());
        e.setJoiningDate(req.getJoiningDate());
        e.setSalary(req.getSalary());

        if (req.getRole() != null)
            e.setRole(Employee.Role.valueOf(req.getRole()));
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.getDepartmentId()));
            e.setDepartment(dept);
        }
        if (req.getDesignationId() != null) {
            Designation desig = designationRepository.findById(req.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", req.getDesignationId()));
            e.setDesignation(desig);
        }
        if (req.getManagerId() != null) {
            Employee manager = employeeRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", req.getManagerId()));
            e.setManager(manager);
        }

        auditLogService.log(updatedBy, "Updated employee: " + e.getName(), "Employee", id);
        return employeeMapper.toDto(employeeRepository.save(e));
    }

    @Override
    @Transactional
    public void updateProfile(Long employeeId, UpdateProfileRequest req, Employee currentUser) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (req.getPhone() != null)
            e.setPhone(req.getPhone());
        if (req.getAddress() != null)
            e.setAddress(req.getAddress());
        if (req.getEmergencyContact() != null)
            e.setEmergencyContact(req.getEmergencyContact());

        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(req.getCurrentPassword(), e.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            e.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        employeeRepository.save(e);
    }

    @Override
    @Transactional
    public void deactivateEmployee(Long id, Employee deactivatedBy) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        e.setStatus(Employee.EmployeeStatus.INACTIVE);
        employeeRepository.save(e);
        auditLogService.log(deactivatedBy, "Deactivated employee: " + e.getName(), "Employee", id);
    }

    @Override
    @Transactional
    public void reactivateEmployee(Long id, Employee reactivatedBy) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        e.setStatus(Employee.EmployeeStatus.ACTIVE);
        employeeRepository.save(e);
        auditLogService.log(reactivatedBy, "Reactivated employee: " + e.getName(), "Employee", id);
    }

    @Override
    @Transactional
    public void changeManager(Long employeeId, Long managerId, Employee changedBy) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        Employee newManager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", managerId));
        e.setManager(newManager);
        employeeRepository.save(e);
        auditLogService.log(changedBy, "Changed manager of " + e.getName() + " to " + newManager.getName(), "Employee",
                employeeId);
    }
}
