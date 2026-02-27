package com.rev.app.rest;

import com.rev.app.dto.AuditLogDTO;
import com.rev.app.dto.CreateEmployeeRequest;
import com.rev.app.dto.EmployeeDTO;
import com.rev.app.entity.*;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.AuditLogMapper;
import com.rev.app.mapper.AnnouncementMapper;
import com.rev.app.dto.AnnouncementDTO;
import com.rev.app.repository.*;
import com.rev.app.service.IAuditLogService;
import com.rev.app.service.IAuthService;
import com.rev.app.service.IEmployeeService;
import com.rev.app.service.ILeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IAuthService authService;
    private final IEmployeeService employeeService;
    private final ILeaveService leaveService;
    private final IAuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;
    private final AnnouncementMapper announcementMapper;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AnnouncementRepository announcementRepository;

    // ===================== EMPLOYEE MANAGEMENT =====================

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(employeeService.searchEmployees(search));
        }
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody CreateEmployeeRequest req) {
        Employee admin = authService.getCurrentEmployee();
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(req, admin));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id,
            @Valid @RequestBody CreateEmployeeRequest req) {
        Employee admin = authService.getCurrentEmployee();
        return ResponseEntity.ok(employeeService.updateEmployee(id, req, admin));
    }

    @PutMapping("/employees/{id}/deactivate")
    public ResponseEntity<String> deactivateEmployee(@PathVariable Long id) {
        Employee admin = authService.getCurrentEmployee();
        employeeService.deactivateEmployee(id, admin);
        return ResponseEntity.ok("Employee deactivated");
    }

    @PutMapping("/employees/{id}/reactivate")
    public ResponseEntity<String> reactivateEmployee(@PathVariable Long id) {
        Employee admin = authService.getCurrentEmployee();
        employeeService.reactivateEmployee(id, admin);
        return ResponseEntity.ok("Employee reactivated");
    }

    @PutMapping("/employees/{id}/change-manager")
    public ResponseEntity<String> changeManager(@PathVariable Long id, @RequestParam Long managerId) {
        Employee admin = authService.getCurrentEmployee();
        employeeService.changeManager(id, managerId, admin);
        return ResponseEntity.ok("Manager updated");
    }

    // ===================== LEAVE BALANCE MANAGEMENT =====================

    @GetMapping("/leaves/balance/{employeeId}")
    public ResponseEntity<LeaveBalance> getLeaveBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
    }

    @PutMapping("/leaves/balance/{employeeId}")
    public ResponseEntity<LeaveBalance> adjustLeaveBalance(@PathVariable Long employeeId,
            @RequestParam(required = false) Integer casualLeave,
            @RequestParam(required = false) Integer sickLeave,
            @RequestParam(required = false) Integer paidLeave,
            @RequestParam String reason) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveBalance", "employeeId", employeeId));
        if (casualLeave != null)
            balance.setCasualLeave(casualLeave);
        if (sickLeave != null)
            balance.setSickLeave(sickLeave);
        if (paidLeave != null)
            balance.setPaidLeave(paidLeave);
        leaveBalanceRepository.save(balance);
        Employee admin = authService.getCurrentEmployee();
        auditLogService.log(admin, "Adjusted leave balance for employee #" + employeeId + " - Reason: " + reason,
                "LeaveBalance", employeeId);
        return ResponseEntity.ok(balance);
    }

    // ===================== DEPARTMENTS =====================

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestParam String name) {
        if (departmentRepository.existsByDeptNameIgnoreCase(name)) {
            throw new BadRequestException("Department already exists: " + name);
        }
        Department dept = new Department(null, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentRepository.save(dept));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestParam String name) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setDeptName(name);
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Department deleted");
    }

    // ===================== DESIGNATIONS =====================

    @GetMapping("/designations")
    public ResponseEntity<List<Designation>> getDesignations() {
        return ResponseEntity.ok(designationRepository.findAll());
    }

    @PostMapping("/designations")
    public ResponseEntity<Designation> createDesignation(@RequestParam String name) {
        if (designationRepository.existsByDesigNameIgnoreCase(name)) {
            throw new BadRequestException("Designation already exists: " + name);
        }
        Designation desig = new Designation(null, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(designationRepository.save(desig));
    }

    @PutMapping("/designations/{id}")
    public ResponseEntity<Designation> updateDesignation(@PathVariable Long id, @RequestParam String name) {
        Designation desig = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        desig.setDesigName(name);
        return ResponseEntity.ok(designationRepository.save(desig));
    }

    @DeleteMapping("/designations/{id}")
    public ResponseEntity<String> deleteDesignation(@PathVariable Long id) {
        designationRepository.deleteById(id);
        return ResponseEntity.ok("Designation deleted");
    }

    // ===================== ANNOUNCEMENTS =====================

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements() {
        return ResponseEntity.ok(
                announcementMapper.toDtoList(announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc()));
    }

    @PostMapping("/announcements")
    public ResponseEntity<AnnouncementDTO> createAnnouncement(@RequestParam String title,
            @RequestParam String content) {
        Employee admin = authService.getCurrentEmployee();
        Announcement a = new Announcement(null, title, content, admin, null, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementMapper.toDto(announcementRepository.save(a)));
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementDTO> updateAnnouncement(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        a.setTitle(title);
        a.setContent(content);
        return ResponseEntity.ok(announcementMapper.toDto(announcementRepository.save(a)));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        a.setIsActive(false);
        announcementRepository.save(a);
        return ResponseEntity.ok("Announcement removed");
    }

    // ===================== AUDIT LOGS =====================

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs() {
        return ResponseEntity.ok(auditLogMapper.toDtoList(auditLogService.getAllLogs()));
    }
}
