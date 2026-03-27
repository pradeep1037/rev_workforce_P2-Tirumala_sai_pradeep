package com.rev.app.service;

import com.rev.app.dto.GoalDTO;
import com.rev.app.dto.GoalRequest;
import com.rev.app.dto.ManagerFeedbackRequest;
import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.dto.PerformanceReviewRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.Goal;
import com.rev.app.entity.PerformanceReview;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.GoalMapper;
import com.rev.app.mapper.PerformanceReviewMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.GoalRepository;
import com.rev.app.repository.PerformanceReviewRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PerformanceServiceTest {

    @Mock
    private PerformanceReviewRepository reviewRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private PerformanceReviewMapper performanceReviewMapper;

    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private PerformanceService performanceService;

    private Employee employee;
    private Employee manager;
    private PerformanceReview review;
    private PerformanceReviewDTO reviewDTO;
    private Goal goal;
    private GoalDTO goalDTO;

    @Before
    public void setUp() {
        manager = new Employee();
        manager.setEmployeeId(2L);
        manager.setName("Jane Manager");

        employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setName("John Doe");
        employee.setManager(manager);

        review = new PerformanceReview();
        review.setReviewId(10L);
        review.setEmployee(employee);
        review.setYear(2024);
        review.setStatus(PerformanceReview.ReviewStatus.DRAFT);

        reviewDTO = new PerformanceReviewDTO();
        reviewDTO.setReviewId(10L);
        reviewDTO.setStatus("DRAFT");

        goal = new Goal();
        goal.setGoalId(100L);
        goal.setEmployee(employee);
        goal.setGoalDescription("Test Goal");
        goal.setPriority(Goal.Priority.HIGH);
        goal.setStatus(Goal.GoalStatus.NOT_STARTED);
        goal.setProgressPercent(0);

        goalDTO = new GoalDTO();
        goalDTO.setGoalId(100L);
        goalDTO.setGoalDescription("Test Goal");
    }

    @Test
    public void testCreateOrUpdateReview_NewReview() {
        PerformanceReviewRequest req = new PerformanceReviewRequest();
        req.setYear(2024);
        req.setSelfRating(4);

        when(reviewRepository.findByEmployeeEmployeeIdAndYear(1L, 2024)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        when(performanceReviewMapper.toDto(any(PerformanceReview.class))).thenReturn(reviewDTO);

        PerformanceReviewDTO result = performanceService.createOrUpdateReview(1L, req, employee);

        assertNotNull(result);
        assertEquals(Long.valueOf(10L), result.getReviewId());
        verify(reviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test(expected = BadRequestException.class)
    public void testCreateOrUpdateReview_AlreadySubmitted() {
        PerformanceReviewRequest req = new PerformanceReviewRequest();
        req.setYear(2024);

        review.setStatus(PerformanceReview.ReviewStatus.SUBMITTED);
        when(reviewRepository.findByEmployeeEmployeeIdAndYear(1L, 2024)).thenReturn(Optional.of(review));

        performanceService.createOrUpdateReview(1L, req, employee);
    }

    @Test
    public void testSubmitReview() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        PerformanceReviewDTO submittedDTO = new PerformanceReviewDTO();
        submittedDTO.setReviewId(10L);
        submittedDTO.setStatus("SUBMITTED");
        when(performanceReviewMapper.toDto(any(PerformanceReview.class))).thenReturn(submittedDTO);

        PerformanceReviewDTO result = performanceService.submitReview(10L, employee);

        assertEquals("SUBMITTED", result.getStatus());
        assertEquals(PerformanceReview.ReviewStatus.SUBMITTED, review.getStatus());
        verify(reviewRepository, times(1)).save(review);
        verify(notificationService, times(1)).send(eq(manager), anyString(), any(), eq(10L));
    }

    @Test
    public void testProvideFeedback() {
        review.setStatus(PerformanceReview.ReviewStatus.SUBMITTED);
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        ManagerFeedbackRequest req = new ManagerFeedbackRequest();
        req.setManagerRating(5);
        req.setManagerFeedback("Great job!");

        PerformanceReviewDTO reviewedDTO = new PerformanceReviewDTO();
        reviewedDTO.setReviewId(10L);
        reviewedDTO.setStatus("REVIEWED");
        when(performanceReviewMapper.toDto(any(PerformanceReview.class))).thenReturn(reviewedDTO);

        PerformanceReviewDTO result = performanceService.provideFeedback(10L, req, manager);

        assertEquals("REVIEWED", result.getStatus());
        assertEquals(Integer.valueOf(5), review.getManagerRating());
        assertEquals("Great job!", review.getManagerFeedback());
        assertEquals(PerformanceReview.ReviewStatus.REVIEWED, review.getStatus());
    }

    @Test(expected = BadRequestException.class)
    public void testProvideFeedback_NotSubmitted() {
        review.setStatus(PerformanceReview.ReviewStatus.DRAFT);
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        ManagerFeedbackRequest req = new ManagerFeedbackRequest();
        req.setManagerRating(5);

        performanceService.provideFeedback(10L, req, manager);
    }

    @Test
    public void testCreateGoal() {
        GoalRequest req = new GoalRequest();
        req.setGoalDescription("New Goal");
        req.setPriority("HIGH");
        req.setDeadline(LocalDate.now().plusDays(10));

        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(goalMapper.toDto(any(Goal.class))).thenReturn(goalDTO);

        GoalDTO result = performanceService.createGoal(1L, req, employee);

        assertNotNull(result);
        verify(goalRepository, times(1)).save(any(Goal.class));
    }

    @Test
    public void testAssignGoal() {
        GoalRequest req = new GoalRequest();
        req.setEmployeeId(1L);
        req.setGoalDescription("Assigned Goal");
        req.setPriority("MEDIUM");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(goalMapper.toDto(any(Goal.class))).thenReturn(goalDTO);

        GoalDTO result = performanceService.assignGoal(2L, req, manager);

        assertNotNull(result);
        verify(goalRepository, times(1)).save(any(Goal.class));
        verify(notificationService, times(1)).send(eq(employee), anyString(), any(), any());
    }

    @Test
    public void testAssignGoal_AsAdmin_Success() {
        Employee admin = new Employee();
        admin.setEmployeeId(99L);
        admin.setRole(Employee.Role.ADMIN);

        Employee anotherEmployee = new Employee();
        anotherEmployee.setEmployeeId(3L);
        // This employee does NOT have 'admin' as manager

        GoalRequest req = new GoalRequest();
        req.setEmployeeId(3L);
        req.setGoalDescription("Admin Assigned Goal");
        req.setPriority("LOW");

        when(employeeRepository.findById(3L)).thenReturn(Optional.of(anotherEmployee));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(goalMapper.toDto(any(Goal.class))).thenReturn(goalDTO);

        GoalDTO result = performanceService.assignGoal(99L, req, admin);

        assertNotNull(result);
        verify(goalRepository, times(1)).save(any(Goal.class));
    }

    @Test(expected = BadRequestException.class)
    public void testAssignGoal_NotDirectReport() {
        Employee anotherManager = new Employee();
        anotherManager.setEmployeeId(3L);

        GoalRequest req = new GoalRequest();
        req.setEmployeeId(1L); // employee has manager ID 2L

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        performanceService.assignGoal(3L, req, anotherManager);
    }

    @Test
    public void testUpdateGoalProgress() {
        when(goalRepository.findById(100L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalDTO updatedDTO = new GoalDTO();
        updatedDTO.setGoalId(100L);
        updatedDTO.setProgressPercent(50);
        updatedDTO.setStatus("IN_PROGRESS");
        when(goalMapper.toDto(any(Goal.class))).thenReturn(updatedDTO);

        doAnswer(invocation -> {
            Goal g = invocation.getArgument(0);
            Integer p = invocation.getArgument(1);
            String s = invocation.getArgument(2);
            g.setProgressPercent(p);
            g.setStatus(Goal.GoalStatus.valueOf(s));
            return null;
        }).when(goalMapper).updateProgress(any(Goal.class), anyInt(), anyString());

        GoalDTO result = performanceService.updateGoalProgress(100L, 50, "IN_PROGRESS", employee);

        assertEquals(Integer.valueOf(50), result.getProgressPercent());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(goalRepository, times(1)).save(goal);
    }
}
