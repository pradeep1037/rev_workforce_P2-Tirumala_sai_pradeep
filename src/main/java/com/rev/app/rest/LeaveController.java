package com.rev.app.rest;

import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.Holiday;
import com.rev.app.entity.LeaveBalance;
import com.rev.app.service.IAuthService;
import com.rev.app.service.ILeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final IAuthService authService;
    private final ILeaveService leaveService;

    @PostMapping("/apply")
    public ResponseEntity<LeaveApplicationDTO> applyLeave(@Valid @RequestBody LeaveRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(current.getEmployeeId(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveApplicationDTO>> getMyLeaves() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(leaveService.getMyLeaves(current.getEmployeeId()));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<String> cancelLeave(@PathVariable Long id) {
        Employee current = authService.getCurrentEmployee();
        leaveService.cancelLeave(id, current.getEmployeeId());
        return ResponseEntity.ok("Leave cancelled successfully");
    }

    @GetMapping("/balance")
    public ResponseEntity<LeaveBalance> getMyBalance() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(leaveService.getLeaveBalance(current.getEmployeeId()));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getHolidays() {
        return ResponseEntity.ok(leaveService.getHolidays());
    }



    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<LeaveApplicationDTO>> getTeamLeaves() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(leaveService.getTeamLeaves(current.getEmployeeId()));
    }

    @GetMapping("/team/balances")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<LeaveBalance>> getTeamLeaveBalances() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(leaveService.getTeamLeaveBalances(current.getEmployeeId()));
    }

    @PutMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<LeaveApplicationDTO> processLeave(@PathVariable Long id,
            @Valid @RequestBody LeaveActionRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(leaveService.processLeave(id, req, current));
    }

    // ===================== ADMIN ENDPOINTS ==================

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveApplicationDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Holiday> addHoliday(@RequestParam String name,
            @RequestParam String date) {
        Holiday h = leaveService.addHoliday(name, LocalDate.parse(date));
        return ResponseEntity.status(HttpStatus.CREATED).body(h);
    }

    @PutMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Holiday> updateHoliday(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String date) {
        Holiday h = leaveService.updateHoliday(id, name, LocalDate.parse(date));
        return ResponseEntity.ok(h);
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long id) {
        leaveService.deleteHoliday(id);
        return ResponseEntity.ok("Holiday deleted");
    }
}
