package com.rev.app.integration;

import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.entity.LeaveBalance;
import com.rev.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-module integration tests for the full leave lifecycle.
 *
 * Modules exercised together: Employee ↔ Leave ↔ LeaveBalance ↔ Notification ↔
 * AuditLog
 *
 * @BeforeEach resets the employee's leave balance to known values (12/6/15)
 *             so each test is independent even with a shared H2 database.
 *
 *             Seeded credentials (DataInitializer):
 *             admin@revworkforce.com / Admin@123
 *             manager@revworkforce.com / Manager@123 (direct manager of
 *             employee)
 *             employee@revworkforce.com / Employee@123 (reports to manager)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeaveWorkflowIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private EmployeeRepository employeeRepository;

        private String adminToken;
        private String managerToken;
        private String employeeToken;
        private Long employeeId;

        // -----------------------------------------------------------------------
        // Setup
        // -----------------------------------------------------------------------

        @BeforeEach
        void setUp() {
                adminToken = "Bearer " + login("admin@revworkforce.com", "Admin@123");
                managerToken = "Bearer " + login("manager@revworkforce.com", "Manager@123");
                employeeToken = "Bearer " + login("employee@revworkforce.com", "Employee@123");

                employeeId = employeeRepository.findByEmail("employee@revworkforce.com")
                                .orElseThrow().getEmployeeId();

                // Reset balance to a known state before each test
                restTemplate.exchange(
                                "/api/admin/leaves/balance/" + employeeId
                                                + "?casualLeave=12&sickLeave=6&paidLeave=15&reason=Test+reset",
                                HttpMethod.PUT, new HttpEntity<>(headersAdmin()), String.class);
        }

        private String login(String email, String password) {
                LoginRequest req = new LoginRequest();
                req.setEmail(email);
                req.setPassword(password);
                ResponseEntity<LoginResponse> res = restTemplate.postForEntity("/api/auth/login", req,
                                LoginResponse.class);
                assertNotNull(res.getBody(), "Login failed for: " + email);
                return res.getBody().getToken();
        }

        private LeaveBalance fetchBalance() {
                ResponseEntity<LeaveBalance> res = restTemplate.exchange(
                                "/api/leaves/balance", HttpMethod.GET,
                                new HttpEntity<>(headersEmployee()), LeaveBalance.class);
                assertEquals(HttpStatus.OK, res.getStatusCode(), "Balance fetch must succeed");
                return res.getBody();
        }

        private HttpHeaders headers(String token) {
                HttpHeaders h = new HttpHeaders();
                h.set("Authorization", token);
                h.setContentType(MediaType.APPLICATION_JSON);
                return h;
        }

        private HttpHeaders headersAdmin() {
                return headers(adminToken);
        }

        private HttpHeaders headersManager() {
                return headers(managerToken);
        }

        private HttpHeaders headersEmployee() {
                return headers(employeeToken);
        }

        // =======================================================================
        // Test 1: Employee applies leave → balance deducted → status PENDING
        // =======================================================================

        @Test
        void applyLeave_BalanceDeductedAndStatusPending() {
                int casualBefore = fetchBalance().getCasualLeave(); // reset = 12

                // Apply 2-day casual leave
                LeaveRequest req = buildLeaveRequest("CASUAL_LEAVE",
                                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
                ResponseEntity<LeaveApplicationDTO> applyRes = restTemplate.exchange(
                                "/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), LeaveApplicationDTO.class);

                assertEquals(HttpStatus.CREATED, applyRes.getStatusCode());
                assertNotNull(applyRes.getBody());
                assertEquals("PENDING", applyRes.getBody().getStatus());
                assertNotNull(applyRes.getBody().getLeaveId());

                // Balance must be reduced by 2
                assertEquals(casualBefore - 2, fetchBalance().getCasualLeave());
        }

        // =======================================================================
        // Test 2: Manager can view team leaves (sees the pending application)
        // =======================================================================

        @Test
        @SuppressWarnings("unchecked")
        void managerCanSeeEmployeePendingLeave() {
                // Employee applies a leave
                LeaveRequest req = buildLeaveRequest("SICK_LEAVE",
                                LocalDate.now().plusDays(10), LocalDate.now().plusDays(10));
                restTemplate.exchange("/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), LeaveApplicationDTO.class);

                // Manager fetches team leaves — returns a list of LeaveApplicationDTOs
                ResponseEntity<List> teamRes = restTemplate.exchange(
                                "/api/leaves/team", HttpMethod.GET,
                                new HttpEntity<>(headersManager()), List.class);

                assertEquals(HttpStatus.OK, teamRes.getStatusCode());
                assertNotNull(teamRes.getBody());
                assertFalse(teamRes.getBody().isEmpty(), "Manager should see at least one team leave");
        }

        // =======================================================================
        // Test 3: Full approval flow — employee applies → manager approves →
        // notification
        // =======================================================================

        @Test
        @SuppressWarnings("unchecked")
        void fullApprovalFlow_EmployeeAppliesToManagerApproves() {
                // Employee applies
                LeaveRequest req = buildLeaveRequest("PAID_LEAVE",
                                LocalDate.now().plusDays(15), LocalDate.now().plusDays(16));
                ResponseEntity<LeaveApplicationDTO> applyRes = restTemplate.exchange(
                                "/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), LeaveApplicationDTO.class);
                assertEquals(HttpStatus.CREATED, applyRes.getStatusCode());
                Long leaveId = applyRes.getBody().getLeaveId();

                // Manager approves
                LeaveActionRequest action = buildAction("APPROVE", "Approved — enjoy your leave");
                ResponseEntity<LeaveApplicationDTO> approveRes = restTemplate.exchange(
                                "/api/leaves/" + leaveId + "/action", HttpMethod.PUT,
                                new HttpEntity<>(action, headersManager()), LeaveApplicationDTO.class);

                assertEquals(HttpStatus.OK, approveRes.getStatusCode());
                assertEquals("APPROVED", approveRes.getBody().getStatus());

                // Employee should have at least one notification
                ResponseEntity<List> notifications = restTemplate.exchange(
                                "/api/employees/notifications", HttpMethod.GET,
                                new HttpEntity<>(headersEmployee()), List.class);
                assertEquals(HttpStatus.OK, notifications.getStatusCode());
                assertFalse(notifications.getBody().isEmpty(),
                                "Employee should have received an approval notification");
        }

        // =======================================================================
        // Test 4: Rejection flow — balance restored after manager rejects
        // =======================================================================

        @Test
        void rejectionFlow_BalanceRestoredAfterManagerRejects() {
                int sickBefore = fetchBalance().getSickLeave(); // reset = 6

                // Apply 1-day sick leave
                LeaveRequest req = buildLeaveRequest("SICK_LEAVE",
                                LocalDate.now().plusDays(20), LocalDate.now().plusDays(20));
                ResponseEntity<LeaveApplicationDTO> applyRes = restTemplate.exchange(
                                "/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), LeaveApplicationDTO.class);
                assertEquals(HttpStatus.CREATED, applyRes.getStatusCode());
                Long leaveId = applyRes.getBody().getLeaveId();

                // Confirm balance was deducted by 1
                assertEquals(sickBefore - 1, fetchBalance().getSickLeave());

                // Manager rejects
                LeaveActionRequest reject = buildAction("REJECT", "Business critical week");
                ResponseEntity<LeaveApplicationDTO> rejectRes = restTemplate.exchange(
                                "/api/leaves/" + leaveId + "/action", HttpMethod.PUT,
                                new HttpEntity<>(reject, headersManager()), LeaveApplicationDTO.class);
                assertEquals(HttpStatus.OK, rejectRes.getStatusCode());
                assertEquals("REJECTED", rejectRes.getBody().getStatus());

                // Balance must be fully restored
                assertEquals(sickBefore, fetchBalance().getSickLeave(),
                                "Sick leave balance must be restored after rejection");
        }

        // =======================================================================
        // Test 5: Cancel flow — balance restored after employee cancels
        // =======================================================================

        @Test
        void cancelFlow_BalanceRestoredAfterEmployeeCancels() {
                int paidBefore = fetchBalance().getPaidLeave(); // reset = 15

                // Apply 3-day paid leave
                LeaveRequest req = buildLeaveRequest("PAID_LEAVE",
                                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32));
                ResponseEntity<LeaveApplicationDTO> applyRes = restTemplate.exchange(
                                "/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), LeaveApplicationDTO.class);
                assertEquals(HttpStatus.CREATED, applyRes.getStatusCode());
                Long leaveId = applyRes.getBody().getLeaveId();

                // Cancel the leave
                ResponseEntity<String> cancelRes = restTemplate.exchange(
                                "/api/leaves/" + leaveId + "/cancel", HttpMethod.DELETE,
                                new HttpEntity<>(headersEmployee()), String.class);
                assertEquals(HttpStatus.OK, cancelRes.getStatusCode());

                // Balance must be restored to pre-apply value
                assertEquals(paidBefore, fetchBalance().getPaidLeave(),
                                "Paid leave balance must be restored after cancellation");
        }

        // =======================================================================
        // Test 6: Admin adjusts leave balance → audit log is created
        // =======================================================================

        @Test
        @SuppressWarnings("unchecked")
        void adminAdjustsLeaveBalance_AuditLogCreated() {
                // Admin adjusts casual leave to 8 (different from reset value of 12)
                ResponseEntity<LeaveBalance> adjRes = restTemplate.exchange(
                                "/api/admin/leaves/balance/" + employeeId + "?casualLeave=8&reason=Manual+adjustment",
                                HttpMethod.PUT, new HttpEntity<>(headersAdmin()), LeaveBalance.class);
                assertEquals(HttpStatus.OK, adjRes.getStatusCode());
                assertEquals(Integer.valueOf(8), adjRes.getBody().getCasualLeave());

                // Admin fetches audit logs — at least one entry must exist
                ResponseEntity<List> logs = restTemplate.exchange(
                                "/api/admin/audit-logs", HttpMethod.GET,
                                new HttpEntity<>(headersAdmin()), List.class);
                assertEquals(HttpStatus.OK, logs.getStatusCode());
                assertFalse(logs.getBody().isEmpty(),
                                "Audit log must contain at least one entry after admin adjustment");
        }

        // =======================================================================
        // Test 7: Employee cannot apply leave with past start date → 400
        // =======================================================================

        @Test
        @SuppressWarnings("unchecked")
        void applyLeave_PastDate_Returns400() {
                LeaveRequest req = buildLeaveRequest("CASUAL_LEAVE",
                                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1));
                ResponseEntity<Map> res = restTemplate.exchange(
                                "/api/leaves/apply", HttpMethod.POST,
                                new HttpEntity<>(req, headersEmployee()), Map.class);

                assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        }

        // =======================================================================
        // Test 8: Regular employee cannot access manager team-leave endpoint → 403
        // =======================================================================

        @Test
        void employee_CannotAccessTeamLeaveEndpoint_Returns403() {
                ResponseEntity<String> res = restTemplate.exchange(
                                "/api/leaves/team", HttpMethod.GET,
                                new HttpEntity<>(headersEmployee()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        }

        // -----------------------------------------------------------------------
        // Helpers
        // -----------------------------------------------------------------------

        private LeaveRequest buildLeaveRequest(String type, LocalDate from, LocalDate to) {
                LeaveRequest req = new LeaveRequest();
                req.setLeaveType(type);
                req.setFromDate(from);
                req.setToDate(to);
                req.setReason("Integration test reason");
                return req;
        }

        private LeaveActionRequest buildAction(String action, String comments) {
                LeaveActionRequest req = new LeaveActionRequest();
                req.setAction(action);
                req.setComments(comments);
                return req;
        }
}
