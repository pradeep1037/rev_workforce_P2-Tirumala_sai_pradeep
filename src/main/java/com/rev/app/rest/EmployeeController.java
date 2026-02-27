package com.rev.app.rest;

import com.rev.app.dto.DashboardDTO;
import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.UpdateProfileRequest;
import com.rev.app.entity.Employee;
import com.rev.app.mapper.EmployeeMapper;
import com.rev.app.service.IAuthService;
import com.rev.app.service.IDashboardService;
import com.rev.app.service.IEmployeeService;
import com.rev.app.service.INotificationService;
import com.rev.app.dto.NotificationDTO;
import com.rev.app.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IAuthService authService;
    private final IEmployeeService employeeService;
    private final INotificationService notificationService;
    private final IDashboardService dashboardService;
    private final EmployeeMapper employeeMapper;
    private final NotificationMapper notificationMapper;

    @GetMapping("/me")
    public ResponseEntity<EmployeeDTO> getMyProfile() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(employeeMapper.toDto(current));
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyProfile(@RequestBody UpdateProfileRequest req) {
        Employee current = authService.getCurrentEmployee();
        employeeService.updateProfile(current.getEmployeeId(), req, current);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(dashboardService.getDashboard(current));
    }

    @GetMapping("/directory")
    public ResponseEntity<List<EmployeeDTO>> getDirectory(@RequestParam(required = false) String search) {
        List<EmployeeDTO> list = (search != null && !search.isBlank())
                ? employeeService.searchEmployees(search)
                : employeeService.getActiveEmployees();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(
                notificationMapper.toDtoList(notificationService.getNotifications(current.getEmployeeId())));
    }

    @PutMapping("/notifications/mark-read")
    public ResponseEntity<String> markAllRead() {
        Employee current = authService.getCurrentEmployee();
        notificationService.markAllRead(current.getEmployeeId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<String> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping("/my-team")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getMyTeam() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(employeeService.getDirectReportees(current.getEmployeeId()));
    }
}
