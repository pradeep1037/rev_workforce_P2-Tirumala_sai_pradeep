package com.rev.app.service;

import com.rev.app.dto.GoalDTO;
import com.rev.app.dto.GoalRequest;
import com.rev.app.dto.ManagerFeedbackRequest;
import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.dto.PerformanceReviewRequest;
import com.rev.app.entity.*;
import com.rev.app.exception.BadRequestException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.GoalMapper;
import com.rev.app.mapper.PerformanceReviewMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.GoalRepository;
import com.rev.app.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceService implements IPerformanceService {

    private final PerformanceReviewRepository reviewRepository;
    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;
    private final INotificationService notificationService;
    private final IAuditLogService auditLogService;
    private final PerformanceReviewMapper performanceReviewMapper;
    private final GoalMapper goalMapper;

    // ===================== PERFORMANCE REVIEWS =====================

    @Override
    @Transactional
    public PerformanceReviewDTO createOrUpdateReview(Long employeeId, PerformanceReviewRequest req, Employee employee) {
        PerformanceReview review = reviewRepository
                .findByEmployeeEmployeeIdAndYear(employeeId, req.getYear())
                .orElse(new PerformanceReview());

        if (review.getStatus() == PerformanceReview.ReviewStatus.SUBMITTED
                || review.getStatus() == PerformanceReview.ReviewStatus.REVIEWED) {
            throw new BadRequestException("Review for " + req.getYear() + " has already been submitted.");
        }

        review.setEmployee(employee);
        review.setYear(req.getYear());
        review.setKeyDeliverables(req.getKeyDeliverables());
        review.setAccomplishments(req.getAccomplishments());
        review.setAreasOfImprovement(req.getAreasOfImprovement());
        review.setSelfRating(req.getSelfRating());
        review.setStatus(PerformanceReview.ReviewStatus.DRAFT);

        return performanceReviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public PerformanceReviewDTO submitReview(Long reviewId, Employee employee) {
        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", reviewId));

        if (!review.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new BadRequestException("You can only submit your own reviews");
        }

        review.setStatus(PerformanceReview.ReviewStatus.SUBMITTED);
        reviewRepository.save(review);

        if (employee.getManager() != null) {
            notificationService.send(
                    employee.getManager(),
                    employee.getName() + " has submitted their performance review for " + review.getYear(),
                    Notification.NotificationType.REVIEW_SUBMITTED,
                    reviewId);
        }

        auditLogService.log(employee, "Submitted performance review #" + reviewId, "PerformanceReview", reviewId);
        return performanceReviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public PerformanceReviewDTO provideFeedback(Long reviewId, ManagerFeedbackRequest req, Employee manager) {
        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", reviewId));

        if (review.getStatus() != PerformanceReview.ReviewStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted reviews can receive manager feedback");
        }

        review.setManagerFeedback(req.getManagerFeedback());
        review.setManagerRating(req.getManagerRating());
        review.setStatus(PerformanceReview.ReviewStatus.REVIEWED);
        reviewRepository.save(review);

        notificationService.send(
                review.getEmployee(),
                "Your manager has reviewed your " + review.getYear() + " performance review.",
                Notification.NotificationType.REVIEW_FEEDBACK,
                reviewId);

        auditLogService.log(manager,
                "Provided feedback on review #" + reviewId + " for " + review.getEmployee().getName(),
                "PerformanceReview", reviewId);
        return performanceReviewMapper.toDto(review);
    }

    @Override
    public List<PerformanceReviewDTO> getMyReviews(Long employeeId) {
        return reviewRepository.findByEmployeeEmployeeId(employeeId)
                .stream().map(performanceReviewMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PerformanceReviewDTO> getTeamReviews(Long managerId) {
        return reviewRepository.findByEmployeeManagerEmployeeId(managerId)
                .stream().map(performanceReviewMapper::toDto).collect(Collectors.toList());
    }

    // ===================== GOALS =====================

    @Override
    @Transactional
    public GoalDTO createGoal(Long employeeId, GoalRequest req, Employee employee) {
        Goal goal = new Goal();
        goal.setEmployee(employee);
        goal.setGoalDescription(req.getGoalDescription());
        goal.setDeadline(req.getDeadline());
        goal.setPriority(Goal.Priority.valueOf(req.getPriority()));
        goal.setStatus(Goal.GoalStatus.NOT_STARTED);
        goal.setProgressPercent(0);

        if (req.getReviewId() != null) {
            PerformanceReview review = reviewRepository.findById(req.getReviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", req.getReviewId()));
            if (!review.getEmployee().getEmployeeId().equals(employeeId)) {
                throw new BadRequestException("Review does not belong to the employee");
            }
            goal.setPerformanceReview(review);
        }

        return goalMapper.toDto(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalDTO assignGoal(Long managerId, GoalRequest req, Employee manager) {
        if (req.getEmployeeId() == null) {
            throw new BadRequestException("Employee ID is required to assign a goal");
        }

        Employee targetEmployee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", req.getEmployeeId()));

        if (targetEmployee.getManager() == null || !targetEmployee.getManager().getEmployeeId().equals(managerId)) {
            throw new BadRequestException("You can only assign goals to your direct reportees");
        }

        Goal goal = new Goal();
        goal.setEmployee(targetEmployee);
        goal.setGoalDescription(req.getGoalDescription());
        goal.setDeadline(req.getDeadline());
        goal.setPriority(Goal.Priority.valueOf(req.getPriority()));
        goal.setStatus(Goal.GoalStatus.NOT_STARTED);
        goal.setProgressPercent(0);

        if (req.getReviewId() != null) {
            PerformanceReview review = reviewRepository.findById(req.getReviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", req.getReviewId()));
            if (!review.getEmployee().getEmployeeId().equals(req.getEmployeeId())) {
                throw new BadRequestException("Review does not belong to the target employee");
            }
            goal.setPerformanceReview(review);
        }

        Goal savedGoal = goalRepository.save(goal);

        notificationService.send(
                targetEmployee,
                "Your manager has assigned you a new goal: " + goal.getGoalDescription(),
                Notification.NotificationType.GOAL_ASSIGNED,
                savedGoal.getGoalId());

        return goalMapper.toDto(savedGoal);
    }

    @Override
    @Transactional
    public GoalDTO linkGoalToReview(Long goalId, Long reviewId, Employee employee) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        if (!goal.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new BadRequestException("You can only link your own goals");
        }

        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", reviewId));

        if (!review.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new BadRequestException("You can only link goals to your own reviews");
        }

        if (review.getStatus() == PerformanceReview.ReviewStatus.SUBMITTED ||
                review.getStatus() == PerformanceReview.ReviewStatus.REVIEWED) {
            throw new BadRequestException("Cannot link goal to a submitted/reviewed performance review");
        }

        goal.setPerformanceReview(review);
        return goalMapper.toDto(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalDTO updateGoalProgress(Long goalId, Integer progress, String status, Employee employee) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        if (!goal.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new BadRequestException("You can only update your own goals");
        }

        goalMapper.updateProgress(goal, progress, status);
        return goalMapper.toDto(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalDTO addManagerComment(Long goalId, String comments, Employee manager) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        goal.setManagerComments(comments);
        return goalMapper.toDto(goalRepository.save(goal));
    }

    @Override
    public List<GoalDTO> getMyGoals(Long employeeId) {
        return goalRepository.findByEmployeeEmployeeId(employeeId)
                .stream().map(goalMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<GoalDTO> getTeamGoals(Long managerId) {
        return goalRepository.findByEmployeeManagerEmployeeId(managerId)
                .stream().map(goalMapper::toDto).collect(Collectors.toList());
    }
}
