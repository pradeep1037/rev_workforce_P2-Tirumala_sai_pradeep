package com.rev.app.controller;

import com.rev.app.entity.Employee;
import com.rev.app.repository.AnnouncementRepository;
import com.rev.app.repository.DepartmentRepository;
import com.rev.app.repository.DesignationRepository;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final EmployeeRepository employeeRepository;
    private final IDashboardService dashboardService;
    private final IEmployeeService employeeService;
    private final ILeaveService leaveService;
    private final IPerformanceService performanceService;
    private final INotificationService notificationService;
    private final IAuditLogService auditLogService;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final AnnouncementRepository announcementRepository;

    private Employee currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Employee user = currentUser();
        model.addAttribute("dashboard", dashboardService.getDashboard(user));
        model.addAttribute("announcements", announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        model.addAttribute("currentUser", user);
        return "dashboard";
    }

    // ============ PROFILE ============
    @GetMapping("/profile")
    public String profile(Model model) {
        Employee user = currentUser();
        model.addAttribute("employee", user);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "profile";
    }

    // ============ LEAVE ============
    @GetMapping("/leaves")
    public String myLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("leaves", leaveService.getMyLeaves(user.getEmployeeId()));
        model.addAttribute("balance", leaveService.getLeaveBalance(user.getEmployeeId()));
        model.addAttribute("holidays", leaveService.getHolidays());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "leaves/my-leaves";
    }

    @GetMapping("/leaves/apply")
    public String applyLeaveForm(Model model) {
        Employee user = currentUser();
        model.addAttribute("balance", leaveService.getLeaveBalance(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "leaves/apply-leave";
    }

    @GetMapping("/leaves/team")
    public String teamLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("teamLeaves", leaveService.getTeamLeaves(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "leaves/team-leaves";
    }

    // ============ PERFORMANCE ============
    @GetMapping("/performance/reviews")
    public String myReviews(Model model) {
        Employee user = currentUser();
        model.addAttribute("reviews", performanceService.getMyReviews(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "performance/my-reviews";
    }

    @GetMapping("/performance/goals")
    public String myGoals(Model model) {
        Employee user = currentUser();
        model.addAttribute("goals", performanceService.getMyGoals(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "performance/my-goals";
    }

    @GetMapping("/performance/team-reviews")
    public String teamReviews(Model model) {
        Employee user = currentUser();
        model.addAttribute("reviews", performanceService.getTeamReviews(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "performance/team-reviews";
    }

    // ============ NOTIFICATIONS ============
    @GetMapping("/notifications")
    public String notifications(Model model) {
        Employee user = currentUser();
        model.addAttribute("notifications", notificationService.getNotifications(user.getEmployeeId()));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "notifications";
    }

    // ============ DIRECTORY ============
    @GetMapping("/directory")
    public String directory(@RequestParam(required = false) String search, Model model) {
        Employee user = currentUser();
        if (search != null && !search.isBlank()) {
            model.addAttribute("employees", employeeService.searchEmployees(search));
        } else {
            model.addAttribute("employees", employeeService.getActiveEmployees());
        }
        model.addAttribute("search", search);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "directory";
    }

    // ============ ADMIN ============
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
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "admin/employees";
    }

    @GetMapping("/admin/departments")
    public String adminDepartments(Model model) {
        Employee user = currentUser();
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("designations", designationRepository.findAll());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "admin/departments";
    }

    @GetMapping("/admin/leaves")
    public String adminLeaves(Model model) {
        Employee user = currentUser();
        model.addAttribute("leaves", leaveService.getAllLeaves());
        model.addAttribute("holidays", leaveService.getHolidays());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "admin/leaves";
    }

    @GetMapping("/admin/announcements")
    public String adminAnnouncements(Model model) {
        Employee user = currentUser();
        model.addAttribute("announcements", announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "admin/announcements";
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        Employee user = currentUser();
        model.addAttribute("logs", auditLogService.getAllLogs());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getEmployeeId()));
        return "admin/audit-logs";
    }
}
