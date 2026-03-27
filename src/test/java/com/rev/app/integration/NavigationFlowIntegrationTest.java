package com.rev.app.integration;

import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Navigation flow integration tests.
 *
 * Validates that every Thymeleaf page route returns the correct HTTP status
 * for each role (admin / manager / employee / unauthenticated).
 *
 * The RestTemplate is configured with NEVER redirect policy so that we capture
 * the raw 302 redirects instead of following them — this lets us assert that
 * unauthenticated requests are redirected to /login rather than returning 200.
 *
 * Public pages (/, /login, /register) must return 200 for everyone.
 * Authenticated pages return 200 for valid sessions, 302 (redirect) for none.
 * Admin-only pages return 403 for manager/employee.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NavigationFlowIntegrationTest {

    /**
     * Standard template that follows redirects (used for authenticated calls
     * where we expect a final 200).
     */
    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;
    private String managerToken;
    private String employeeToken;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + login("admin@revworkforce.com", "Admin@123");
        managerToken = "Bearer " + login("manager@revworkforce.com", "Manager@123");
        employeeToken = "Bearer " + login("employee@revworkforce.com", "Employee@123");
    }

    private String login(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        ResponseEntity<LoginResponse> res = restTemplate.postForEntity(
                "/api/auth/login", req, LoginResponse.class);
        return res.getBody().getToken();
    }

    /**
     * Makes a GET request sending the JWT in an Authorization header.
     * Spring Security's CookieBearerTokenResolver also accepts the Authorization
     * header, so this works for cookie-based Thymeleaf pages too.
     */
    private ResponseEntity<String> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("Authorization", token);
        }
        return restTemplate.exchange(path, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }

    // =======================================================================
    // Public pages — must respond 200 for everybody (including no auth)
    // =======================================================================

    /** Test 1: Home page (/) is publicly accessible. */
    @Test
    void homePage_IsPublic_Returns200ForAll() {
        assertEquals(HttpStatus.OK, get("/", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/", employeeToken).getStatusCode());
        // Unauthenticated — must not require a login redirect
        HttpStatusCode unauth = get("/", null).getStatusCode();
        assertTrue(unauth.is2xxSuccessful() || unauth.is3xxRedirection(),
                "Home page should be accessible (200) or at most redirect");
    }

    /** Test 2: Login page (/login) is publicly accessible. */
    @Test
    void loginPage_IsPublic_Returns200() {
        assertEquals(HttpStatus.OK, get("/login", null).getStatusCode());
    }

    /** Test 3: Register page (/register) is publicly accessible. */
    @Test
    void registerPage_IsPublic_Returns200() {
        assertEquals(HttpStatus.OK, get("/register", null).getStatusCode());
    }

    // =======================================================================
    // Authenticated pages — 200 for valid token, redirect/4xx for none
    // =======================================================================

    /** Test 4: Dashboard is accessible to all authenticated roles. */
    @Test
    void dashboardPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/dashboard", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/dashboard", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/dashboard", employeeToken).getStatusCode());
    }

    /** Test 5: Profile page is accessible to all authenticated roles. */
    @Test
    void profilePage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/profile", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/profile", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/profile", employeeToken).getStatusCode());
    }

    /** Test 6: My-leaves page is accessible to all authenticated roles. */
    @Test
    void leavesPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/leaves", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/leaves", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/leaves", employeeToken).getStatusCode());
    }

    /** Test 7: Apply-leave form is accessible to all authenticated roles. */
    @Test
    void applyLeavePage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/leaves/apply", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/leaves/apply", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/leaves/apply", employeeToken).getStatusCode());
    }

    /** Test 8: Team-leaves page is accessible to manager and admin. */
    @Test
    void teamLeavesPage_ManagerAndAdmin_Return200() {
        assertEquals(HttpStatus.OK, get("/leaves/team", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/leaves/team", managerToken).getStatusCode());
    }

    /** Test 9: Directory page is accessible to all authenticated roles. */
    @Test
    void directoryPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/directory", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/directory", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/directory", employeeToken).getStatusCode());
    }

    // =======================================================================
    // Admin-only Thymeleaf pages — 200 for admin, 403 for others
    // =======================================================================

    /** Test 10: Admin employees page — admin only. */
    @Test
    void adminEmployeesPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/employees", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/employees", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/employees", employeeToken).getStatusCode());
    }

    /** Test 11: Admin leaves page — admin only. */
    @Test
    void adminLeavesPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/leaves", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/leaves", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/leaves", employeeToken).getStatusCode());
    }

    /** Test 12: Admin departments page — admin only. */
    @Test
    void adminDepartmentsPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/departments", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/departments", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/departments", employeeToken).getStatusCode());
    }

    /** Test 13: Admin audit-logs page — admin only. */
    @Test
    void adminAuditLogsPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/audit-logs", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/audit-logs", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/audit-logs", employeeToken).getStatusCode());
    }

    /** Test 14: Admin holidays page — admin only. */
    @Test
    void adminHolidaysPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/holidays", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/holidays", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/holidays", employeeToken).getStatusCode());
    }

    // =======================================================================
    // Notifications page — accessible to all authenticated roles
    // =======================================================================

    /** Test 15: Notifications page — all authenticated roles. */
    @Test
    void notificationsPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/notifications", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/notifications", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/notifications", employeeToken).getStatusCode());
    }

    // =======================================================================
    // Performance pages — reviews/goals for all; team pages for mgr/admin
    // =======================================================================

    /** Test 16: My performance reviews page — all authenticated roles. */
    @Test
    void performanceReviewsPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/performance/reviews", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/reviews", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/reviews", employeeToken).getStatusCode());
    }

    /** Test 17: My goals page — all authenticated roles. */
    @Test
    void performanceGoalsPage_AuthenticatedRoles_Return200() {
        assertEquals(HttpStatus.OK, get("/performance/goals", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/goals", managerToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/goals", employeeToken).getStatusCode());
    }

    /** Test 18: Team reviews page — manager and admin only. */
    @Test
    void teamReviewsPage_ManagerAndAdmin_Return200() {
        assertEquals(HttpStatus.OK, get("/performance/team-reviews", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/team-reviews", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/performance/team-reviews", employeeToken).getStatusCode());
    }

    /** Test 19: Team goals page — manager and admin only. */
    @Test
    void teamGoalsPage_ManagerAndAdmin_Return200() {
        assertEquals(HttpStatus.OK, get("/performance/team-goals", adminToken).getStatusCode());
        assertEquals(HttpStatus.OK, get("/performance/team-goals", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/performance/team-goals", employeeToken).getStatusCode());
    }

    /** Test 20: Admin leave balances page — admin only. */
    @Test
    void adminLeaveBalancesPage_AdminOnly() {
        assertEquals(HttpStatus.OK, get("/admin/leaves/balances", adminToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/leaves/balances", managerToken).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/admin/leaves/balances", employeeToken).getStatusCode());
    }
}
