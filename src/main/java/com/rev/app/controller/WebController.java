package com.rev.app.controller;

import com.rev.app.entity.Employee;
import com.rev.app.mapper.NotificationMapper;
import com.rev.app.repository.AnnouncementRepository;
import com.rev.app.repository.DepartmentRepository;
import com.rev.app.repository.DesignationRepository;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.LeaveBalanceRepository;
import com.rev.app.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final EmployeeRepository employeeRepository;
    private final IDashboardService dashboardService;
    private final IEmployeeService employeeService;
    private final ILeaveService leaveService;
    private final IAuditLogService auditLogService;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final AnnouncementRepository announcementRepository;
    private final INotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final IPerformanceService performanceService;
    private final LeaveBalanceRepository leaveBalanceRepository;

    private Employee currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Employee user = currentUser();
        model.addAttribute("dashboard", dashboardService.getDashboard(user));
        model.addAttribute("announcements", announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("currentUser", user);
        model.addAttribute("holidays", leaveService.getHolidays());
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Employee user = currentUser();
        model.addAttribute("employee", user);
        return "profile";
    }

    @GetMapping("/my-team")
    public String myTeam(Model model) {
        Employee user = currentUser();
        model.addAttribute("team", employeeService.getDirectReportees(user.getEmployeeId()));
        return "my-team";
    }

    @GetMapping("/leaves")
    public String myLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("leaves", leaveService.getMyLeaves(user.getEmployeeId()));
        model.addAttribute("balance", leaveService.getLeaveBalance(user.getEmployeeId()));
        model.addAttribute("holidays", leaveService.getHolidays());
        return "leaves/my-leaves";
    }

    @GetMapping("/leaves/apply")
    public String applyLeaveForm(Model model) {
        Employee user = currentUser();
        model.addAttribute("balance", leaveService.getLeaveBalance(user.getEmployeeId()));
        return "leaves/apply-leave";
    }

    @GetMapping("/leaves/team")
    public String teamLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("teamLeaves", leaveService.getTeamLeaves(user.getEmployeeId()));
        return "leaves/team-leaves";
    }

    @GetMapping("/directory")
    public String directory(@RequestParam(required = false) String search, Model model) {
        Employee user = currentUser();
        if (search != null && !search.isBlank()) {
            model.addAttribute("employees", employeeService.searchEmployees(search));
        } else {
            model.addAttribute("employees", employeeService.getActiveEmployees());
        }
        model.addAttribute("search", search);
        return "directory";
    }

    @GetMapping("/admin/employees")
    public String adminEmployees(@RequestParam(required = false) String search, Model model) {
        Employee user = currentUser();
        if (search != null && !search.isBlank()) {
            model.addAttribute("employees", employeeService.searchEmployees(search));
        } else {
            model.addAttribute("employees", employeeService.getAllEmployees());
        }
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("designations", designationRepository.findAll());
        model.addAttribute("managers", employeeRepository.findByRole(Employee.Role.MANAGER));
        return "admin/employees";
    }

    @GetMapping("/admin/departments")
    public String adminDepartments(Model model) {
        Employee user = currentUser();
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("designations", designationRepository.findAll());
        return "admin/departments";
    }

    @GetMapping("/admin/leaves")
    public String adminLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("leaves", leaveService.getAllLeaves());
        return "admin/leaves";
    }

    @GetMapping("/admin/holidays")
    public String adminHolidays(Model model) {
        Employee user = currentUser();
        model.addAttribute("holidays", leaveService.getHolidays());
        return "admin/holidays";
    }

    @GetMapping("/admin/announcements")
    public String adminAnnouncements(Model model) {
        Employee user = currentUser();
        model.addAttribute("announcements", announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        return "admin/announcements";
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        Employee user = currentUser();
        model.addAttribute("logs", auditLogService.getAllLogs());
        return "admin/audit-logs";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        Employee user = currentUser();
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/reports";
    }

    @GetMapping("/admin/leaves/balances")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminLeaveBalances(Model model) {
        model.addAttribute("balances", leaveBalanceRepository.findAllWithEmployee());
        return "admin/leave-balances";
    }

    // ===================== NOTIFICATIONS =====================

    @GetMapping("/notifications")
    public String notifications(Model model) {
        Employee user = currentUser();
        var notifications = notificationService.getNotifications(user.getEmployeeId());
        long unreadCount = notifications.stream().filter(n -> !n.getIsRead()).count();
        model.addAttribute("notifications", notificationMapper.toDtoList(notifications));
        model.addAttribute("unreadCount", unreadCount);
        return "notifications";
    }

    // ===================== PERFORMANCE =====================

    @GetMapping("/performance/reviews")
    public String myPerformanceReviews(Model model) {
        Employee user = currentUser();
        model.addAttribute("reviews", performanceService.getMyReviews(user.getEmployeeId()));
        return "performance/my-reviews";
    }

    @GetMapping("/performance/goals")
    public String myGoals(Model model) {
        Employee user = currentUser();
        model.addAttribute("goals", performanceService.getMyGoals(user.getEmployeeId()));
        return "performance/my-goals";
    }

    @GetMapping("/performance/team-reviews")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String teamReviews(Model model) {
        Employee user = currentUser();
        model.addAttribute("reviews", performanceService.getTeamReviews(user.getEmployeeId()));
        return "performance/team-reviews";
    }

    @GetMapping("/performance/team-goals")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String teamGoals(Model model) {
        Employee user = currentUser();
        model.addAttribute("goals", performanceService.getTeamGoals(user.getEmployeeId()));
        model.addAttribute("teamMembers", employeeService.getDirectReportees(user.getEmployeeId()));
        model.addAttribute("teamReviews", performanceService.getTeamReviews(user.getEmployeeId()));
        return "performance/team-goals";
    }
}
