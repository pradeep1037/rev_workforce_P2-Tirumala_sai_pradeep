package com.rev.app.service;

import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveApplication;
import com.rev.app.entity.LeaveBalance;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.LeaveApplicationMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.HolidayRepository;
import com.rev.app.repository.LeaveApplicationRepository;
import com.rev.app.repository.LeaveBalanceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveService}.
 *
 * Covers:
 * - Balance deduction when applying leave (casual, sick, paid)
 * - Validation: past dates, reversed date range, insufficient balance
 * - Approval workflow: manager approves, manager rejects (balance restored)
 * - Authorization: unauthorized actor, already-processed leave
 * - Self-approval comment requirement
 * - Admin approves another employee's leave
 * - Cancel leave: own PENDING leave restores balance; guards on wrong-owner and
 * non-PENDING
 */
@RunWith(MockitoJUnitRunner.class)
public class LeaveServiceTest {

    // -----------------------------------------------------------------------
    // Mocks
    // -----------------------------------------------------------------------

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private LeaveApplicationMapper leaveApplicationMapper;

    @InjectMocks
    private LeaveService leaveService;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private Employee manager;
    private Employee employee;
    private LeaveBalance balance;
    private LeaveApplication pendingLeave;
    private LeaveApplicationDTO leaveDTO;

    /** Tomorrow and 2 days later — always in the future. */
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate DAY_AFTER = LocalDate.now().plusDays(2); // 2-day leave

    @Before
    public void setUp() {
        manager = new Employee();
        manager.setEmployeeId(10L);
        manager.setName("Alice Manager");
        manager.setRole(Employee.Role.MANAGER);

        employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setName("Bob Employee");
        employee.setRole(Employee.Role.EMPLOYEE);
        employee.setManager(manager);

        // 12 casual / 6 sick / 15 paid
        balance = new LeaveBalance(employee, 12, 6, 15);

        pendingLeave = new LeaveApplication();
        pendingLeave.setLeaveId(100L);
        pendingLeave.setEmployee(employee);
        pendingLeave.setManager(manager);
        pendingLeave.setLeaveType(LeaveApplication.LeaveType.CASUAL_LEAVE);
        pendingLeave.setFromDate(TOMORROW);
        pendingLeave.setToDate(DAY_AFTER);
        pendingLeave.setStatus(LeaveApplication.LeaveStatus.PENDING);
        pendingLeave.setAppliedOn(LocalDateTime.now());

        leaveDTO = new LeaveApplicationDTO();
        leaveDTO.setLeaveId(100L);
        leaveDTO.setStatus("PENDING");
    }

    // =======================================================================
    // applyLeave — happy paths
    // =======================================================================

    /**
     * Test 1: Applying casual leave deducts balance and saves a PENDING
     * application.
     */
    @Test
    public void applyLeave_CasualLeave_DeductsBalanceAndReturnsPending() {
        LeaveRequest req = buildRequest("CASUAL_LEAVE", TOMORROW, DAY_AFTER);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenReturn(pendingLeave);
        when(leaveApplicationMapper.toDto(any(LeaveApplication.class))).thenReturn(leaveDTO);

        LeaveApplicationDTO result = leaveService.applyLeave(1L, req);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        // 2 days deducted from 12 → 10
        assertEquals(Integer.valueOf(10), balance.getCasualLeave());
        verify(leaveBalanceRepository, times(1)).save(balance);
        verify(notificationService, times(1)).send(eq(manager), anyString(), any(), anyLong());
    }

    /** Test 2: Applying sick leave deducts sick-leave balance correctly. */
    @Test
    public void applyLeave_SickLeave_DeductsSickBalance() {
        LeaveRequest req = buildRequest("SICK_LEAVE", TOMORROW, TOMORROW); // 1 day

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(balance));

        LeaveApplication savedLeave = cloneLeave(pendingLeave, LeaveApplication.LeaveType.SICK_LEAVE, TOMORROW,
                TOMORROW);
        LeaveApplicationDTO sickDTO = new LeaveApplicationDTO();
        sickDTO.setLeaveId(101L);
        sickDTO.setStatus("PENDING");

        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenReturn(savedLeave);
        when(leaveApplicationMapper.toDto(any(LeaveApplication.class))).thenReturn(sickDTO);

        leaveService.applyLeave(1L, req);

        // 1 day deducted from 6 → 5
        assertEquals(Integer.valueOf(5), balance.getSickLeave());
    }

    // =======================================================================
    // applyLeave — validation failures
    // =======================================================================

    /** Test 3: From date in the past must throw BadRequestException. */
    @Test(expected = BadRequestException.class)
    public void applyLeave_PastDate_ThrowsBadRequest() {
        LeaveRequest req = buildRequest("CASUAL_LEAVE", LocalDate.now().minusDays(1), LocalDate.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        leaveService.applyLeave(1L, req);
    }

    /** Test 4: From date after to date must throw BadRequestException. */
    @Test(expected = BadRequestException.class)
    public void applyLeave_FromAfterTo_ThrowsBadRequest() {
        LeaveRequest req = buildRequest("CASUAL_LEAVE", DAY_AFTER, TOMORROW); // reversed
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        leaveService.applyLeave(1L, req);
    }

    /** Test 5: Insufficient casual leave balance must throw BadRequestException. */
    @Test(expected = BadRequestException.class)
    public void applyLeave_InsufficientBalance_ThrowsBadRequest() {
        balance = new LeaveBalance(employee, 1, 6, 15); // only 1 casual day left
        LeaveRequest req = buildRequest("CASUAL_LEAVE", TOMORROW, DAY_AFTER); // wants 2 days
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(balance));

        leaveService.applyLeave(1L, req);
    }

    /** Test 6: No leave balance record found must throw BadRequestException. */
    @Test(expected = BadRequestException.class)
    public void applyLeave_NoBalanceRecord_ThrowsBadRequest() {
        LeaveRequest req = buildRequest("CASUAL_LEAVE", TOMORROW, DAY_AFTER);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.empty());

        leaveService.applyLeave(1L, req);
    }

    /** Test 7: Employee not found must throw ResourceNotFoundException. */
    @Test(expected = ResourceNotFoundException.class)
    public void applyLeave_EmployeeNotFound_ThrowsResourceNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        leaveService.applyLeave(99L, buildRequest("CASUAL_LEAVE", TOMORROW, DAY_AFTER));
    }

    // =======================================================================
    // processLeave — approve
    // =======================================================================

    /**
     * Test 8: Manager approves a PENDING leave → status APPROVED, balance NOT
     * restored.
     */
    @Test
    public void processLeave_ManagerApproves_StatusApproved_BalanceNotRestored() {
        LeaveActionRequest req = buildAction("APPROVE", "Good to go");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        when(leaveApplicationRepository.save(any())).thenReturn(pendingLeave);

        LeaveApplicationDTO approvedDTO = new LeaveApplicationDTO();
        approvedDTO.setLeaveId(100L);
        approvedDTO.setStatus("APPROVED");
        when(leaveApplicationMapper.toDto(any())).thenReturn(approvedDTO);

        LeaveApplicationDTO result = leaveService.processLeave(100L, req, manager);

        assertEquals("APPROVED", result.getStatus());
        assertEquals(LeaveApplication.LeaveStatus.APPROVED, pendingLeave.getStatus());
        // Balance must NOT be touched on approval
        verify(leaveBalanceRepository, never()).findByEmployeeEmployeeId(anyLong());
        verify(auditLogService, times(1)).log(eq(manager), anyString(), eq("LeaveApplication"), eq(100L));
    }

    /** Test 9: Admin (not the direct manager) can also approve. */
    @Test
    public void processLeave_AdminApproves_OtherEmployeeLeave_Succeeds() {
        Employee admin = new Employee();
        admin.setEmployeeId(50L);
        admin.setName("Carol Admin");
        admin.setRole(Employee.Role.ADMIN);

        LeaveActionRequest req = buildAction("APPROVE", "Admin override");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        when(leaveApplicationRepository.save(any())).thenReturn(pendingLeave);

        LeaveApplicationDTO approvedDTO = new LeaveApplicationDTO();
        approvedDTO.setStatus("APPROVED");
        when(leaveApplicationMapper.toDto(any())).thenReturn(approvedDTO);

        LeaveApplicationDTO result = leaveService.processLeave(100L, req, admin);

        assertEquals("APPROVED", result.getStatus());
    }

    // =======================================================================
    // processLeave — reject (with balance restoration)
    // =======================================================================

    /** Test 10: Manager rejects leave → status REJECTED, balance restored. */
    @Test
    public void processLeave_ManagerRejects_BalanceRestored() {
        LeaveActionRequest req = buildAction("REJECT", "Not applicable");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        when(leaveApplicationRepository.save(any())).thenReturn(pendingLeave);
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any())).thenReturn(balance);

        LeaveApplicationDTO rejectedDTO = new LeaveApplicationDTO();
        rejectedDTO.setStatus("REJECTED");
        when(leaveApplicationMapper.toDto(any())).thenReturn(rejectedDTO);

        int casualBefore = balance.getCasualLeave(); // 12
        LeaveApplicationDTO result = leaveService.processLeave(100L, req, manager);

        assertEquals("REJECTED", result.getStatus());
        assertEquals(LeaveApplication.LeaveStatus.REJECTED, pendingLeave.getStatus());
        // 2-day leave (TOMORROW → DAY_AFTER) must be credited back
        assertEquals(Integer.valueOf(casualBefore + 2), balance.getCasualLeave());
        verify(leaveBalanceRepository, times(1)).save(balance);
    }

    // =======================================================================
    // processLeave — validation / authorization failures
    // =======================================================================

    /** Test 11: Rejecting without comments must throw BadRequestException. */
    @Test(expected = BadRequestException.class)
    public void processLeave_RejectWithoutComments_ThrowsBadRequest() {
        LeaveActionRequest req = buildAction("REJECT", "");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));

        leaveService.processLeave(100L, req, manager);
    }

    /**
     * Test 12: Processing an already-approved leave must throw BadRequestException.
     */
    @Test(expected = BadRequestException.class)
    public void processLeave_AlreadyApproved_ThrowsBadRequest() {
        pendingLeave.setStatus(LeaveApplication.LeaveStatus.APPROVED);
        LeaveActionRequest req = buildAction("APPROVE", "");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));

        leaveService.processLeave(100L, req, manager);
    }

    /**
     * Test 13: Unauthorized actor (not manager, not admin) must throw
     * BadRequestException.
     */
    @Test(expected = BadRequestException.class)
    public void processLeave_UnauthorizedActor_ThrowsBadRequest() {
        Employee stranger = new Employee();
        stranger.setEmployeeId(99L);
        stranger.setRole(Employee.Role.EMPLOYEE);

        LeaveActionRequest req = buildAction("APPROVE", "");
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));

        leaveService.processLeave(100L, req, stranger);
    }

    /**
     * Test 14: Admin self-approval without a comment must throw
     * BadRequestException.
     */
    @Test(expected = BadRequestException.class)
    public void processLeave_AdminSelfApproval_NoComment_ThrowsBadRequest() {
        // Make 'manager' the applicant as well (self-approval)
        pendingLeave.setEmployee(manager);

        LeaveActionRequest req = buildAction("APPROVE", ""); // no comment
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));

        leaveService.processLeave(100L, req, manager);
    }

    // =======================================================================
    // cancelLeave
    // =======================================================================

    /**
     * Test 15: Employee cancels own PENDING leave → CANCELLED, balance restored.
     */
    @Test
    public void cancelLeave_OwnPendingLeave_CancelledAndBalanceRestored() {
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        when(leaveBalanceRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveApplicationRepository.save(any())).thenReturn(pendingLeave);
        when(leaveBalanceRepository.save(any())).thenReturn(balance);

        int casualBefore = balance.getCasualLeave(); // 12
        leaveService.cancelLeave(100L, 1L);

        assertEquals(LeaveApplication.LeaveStatus.CANCELLED, pendingLeave.getStatus());
        assertEquals(Integer.valueOf(casualBefore + 2), balance.getCasualLeave());
        verify(leaveApplicationRepository, times(1)).save(pendingLeave);
    }

    /**
     * Test 16: Cancelling another employee's leave must throw BadRequestException.
     */
    @Test(expected = BadRequestException.class)
    public void cancelLeave_WrongEmployee_ThrowsBadRequest() {
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        leaveService.cancelLeave(100L, 999L); // wrong owner
    }

    /**
     * Test 17: Cancelling an already-approved leave must throw BadRequestException.
     */
    @Test(expected = BadRequestException.class)
    public void cancelLeave_AlreadyApproved_ThrowsBadRequest() {
        pendingLeave.setStatus(LeaveApplication.LeaveStatus.APPROVED);
        when(leaveApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLeave));
        leaveService.cancelLeave(100L, 1L);
    }

    // =======================================================================
    // Helpers
    // =======================================================================

    private LeaveRequest buildRequest(String type, LocalDate from, LocalDate to) {
        LeaveRequest req = new LeaveRequest();
        req.setLeaveType(type);
        req.setFromDate(from);
        req.setToDate(to);
        req.setReason("Test reason");
        return req;
    }

    private LeaveActionRequest buildAction(String action, String comments) {
        LeaveActionRequest req = new LeaveActionRequest();
        req.setAction(action);
        req.setComments(comments);
        return req;
    }

    private LeaveApplication cloneLeave(LeaveApplication src, LeaveApplication.LeaveType type,
            LocalDate from, LocalDate to) {
        LeaveApplication la = new LeaveApplication();
        la.setLeaveId(src.getLeaveId() + 1);
        la.setEmployee(src.getEmployee());
        la.setManager(src.getManager());
        la.setLeaveType(type);
        la.setFromDate(from);
        la.setToDate(to);
        la.setStatus(LeaveApplication.LeaveStatus.PENDING);
        la.setAppliedOn(LocalDateTime.now());
        return la;
    }
}
