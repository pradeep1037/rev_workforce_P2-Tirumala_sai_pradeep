package com.rev.app.mapper;

import com.rev.app.dto.GoalDTO;
import com.rev.app.entity.Goal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    /**
     * Maps a Goal entity to GoalDTO.
     */
    public GoalDTO toDto(Goal g) {
        if (g == null)
            return null;

        GoalDTO dto = new GoalDTO();
        dto.setGoalId(g.getGoalId());

        if (g.getEmployee() != null) {
            dto.setEmployeeId(g.getEmployee().getEmployeeId());
            dto.setEmployeeName(g.getEmployee().getName());
        }

        dto.setGoalDescription(g.getGoalDescription());
        dto.setDeadline(g.getDeadline());
        dto.setPriority(g.getPriority() != null ? g.getPriority().name() : null);
        dto.setStatus(g.getStatus() != null ? g.getStatus().name() : null);
        dto.setProgressPercent(g.getProgressPercent());
        dto.setManagerComments(g.getManagerComments());

        if (g.getPerformanceReview() != null) {
            dto.setReviewId(g.getPerformanceReview().getReviewId());
        }

        return dto;
    }

    /**
     * Applies progress and status fields from primitive values onto a Goal entity.
     */
    public void updateProgress(Goal g, Integer progress, String status) {
        if (g == null)
            return;
        if (progress != null)
            g.setProgressPercent(progress);
        if (status != null)
            g.setStatus(Goal.GoalStatus.valueOf(status));
    }
}
