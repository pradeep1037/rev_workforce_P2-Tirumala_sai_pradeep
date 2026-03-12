package com.rev.app.rest;

import com.rev.app.dto.GoalDTO;
import com.rev.app.dto.GoalRequest;
import com.rev.app.dto.ManagerFeedbackRequest;
import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.dto.PerformanceReviewRequest;
import com.rev.app.entity.Employee;
import com.rev.app.service.IAuthService;
import com.rev.app.service.IPerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final IAuthService authService;
    private final IPerformanceService performanceService;

    // ===================== REVIEWS =====================

    @PostMapping("/reviews")
    public ResponseEntity<PerformanceReviewDTO> createReview(@Valid @RequestBody PerformanceReviewRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(performanceService.createOrUpdateReview(current.getEmployeeId(), req, current));
    }

    @PostMapping("/reviews/{id}/submit")
    public ResponseEntity<PerformanceReviewDTO> submitReview(@PathVariable Long id) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.submitReview(id, current));
    }

    @GetMapping("/reviews/my")
    public ResponseEntity<List<PerformanceReviewDTO>> getMyReviews() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.getMyReviews(current.getEmployeeId()));
    }

    @GetMapping("/reviews/team")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<PerformanceReviewDTO>> getTeamReviews() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.getTeamReviews(current.getEmployeeId()));
    }

    @PutMapping("/reviews/{id}/feedback")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<PerformanceReviewDTO> provideFeedback(@PathVariable Long id,
            @Valid @RequestBody ManagerFeedbackRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.provideFeedback(id, req, current));
    }

    // ===================== GOALS =====================

    @PostMapping("/goals")
    public ResponseEntity<GoalDTO> createGoal(@Valid @RequestBody GoalRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(performanceService.createGoal(current.getEmployeeId(), req, current));
    }

    @PostMapping("/goals/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<GoalDTO> assignGoal(@Valid @RequestBody GoalRequest req) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(performanceService.assignGoal(current.getEmployeeId(), req, current));
    }

    @PutMapping("/goals/{id}/link-review")
    public ResponseEntity<GoalDTO> linkGoalToReview(@PathVariable Long id, @RequestParam Long reviewId) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.linkGoalToReview(id, reviewId, current));
    }

    @PutMapping("/goals/{id}/progress")
    public ResponseEntity<GoalDTO> updateGoalProgress(@PathVariable Long id,
            @RequestParam Integer progress,
            @RequestParam(required = false) String status) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.updateGoalProgress(id, progress, status, current));
    }

    @GetMapping("/goals/my")
    public ResponseEntity<List<GoalDTO>> getMyGoals() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.getMyGoals(current.getEmployeeId()));
    }

    @GetMapping("/goals/team")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<GoalDTO>> getTeamGoals() {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.getTeamGoals(current.getEmployeeId()));
    }

    @PutMapping("/goals/{id}/comment")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<GoalDTO> addManagerComment(@PathVariable Long id,
            @RequestParam String comments) {
        Employee current = authService.getCurrentEmployee();
        return ResponseEntity.ok(performanceService.addManagerComment(id, comments, current));
    }
}
