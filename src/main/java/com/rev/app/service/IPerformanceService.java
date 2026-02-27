package com.rev.app.service;

import com.rev.app.dto.GoalDTO;
import com.rev.app.dto.GoalRequest;
import com.rev.app.dto.ManagerFeedbackRequest;
import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.dto.PerformanceReviewRequest;
import com.rev.app.entity.Employee;

import java.util.List;

public interface IPerformanceService {

    // Reviews
    PerformanceReviewDTO createOrUpdateReview(Long employeeId, PerformanceReviewRequest req, Employee employee);

    PerformanceReviewDTO submitReview(Long reviewId, Employee employee);

    PerformanceReviewDTO provideFeedback(Long reviewId, ManagerFeedbackRequest req, Employee manager);

    List<PerformanceReviewDTO> getMyReviews(Long employeeId);

    List<PerformanceReviewDTO> getTeamReviews(Long managerId);

    // Goals
    GoalDTO createGoal(Long employeeId, GoalRequest req, Employee employee);

    GoalDTO updateGoalProgress(Long goalId, Integer progress, String status, Employee employee);

    GoalDTO addManagerComment(Long goalId, String comments, Employee manager);

    List<GoalDTO> getMyGoals(Long employeeId);

    List<GoalDTO> getTeamGoals(Long managerId);
}
