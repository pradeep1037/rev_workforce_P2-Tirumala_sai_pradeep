package com.rev.app.mapper;

import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.entity.PerformanceReview;
import org.springframework.stereotype.Component;

@Component
public class PerformanceReviewMapper {

    /**
     * Maps a PerformanceReview entity to PerformanceReviewDTO.
     */
    public PerformanceReviewDTO toDto(PerformanceReview pr) {
        if (pr == null)
            return null;

        PerformanceReviewDTO dto = new PerformanceReviewDTO();
        dto.setReviewId(pr.getReviewId());

        if (pr.getEmployee() != null) {
            dto.setEmployeeId(pr.getEmployee().getEmployeeId());
            dto.setEmployeeName(pr.getEmployee().getName());
        }

        dto.setYear(pr.getYear());
        dto.setKeyDeliverables(pr.getKeyDeliverables());
        dto.setAccomplishments(pr.getAccomplishments());
        dto.setAreasOfImprovement(pr.getAreasOfImprovement());
        dto.setSelfRating(pr.getSelfRating());
        dto.setManagerRating(pr.getManagerRating());
        dto.setManagerFeedback(pr.getManagerFeedback());
        dto.setStatus(pr.getStatus() != null ? pr.getStatus().name() : null);

        return dto;
    }

    /**
     * Applies PerformanceReviewDTO self-assessment fields onto an entity.
     */
    public void updateEntityFromDto(PerformanceReviewDTO dto, PerformanceReview pr) {
        if (dto == null || pr == null)
            return;
        if (dto.getKeyDeliverables() != null)
            pr.setKeyDeliverables(dto.getKeyDeliverables());
        if (dto.getAccomplishments() != null)
            pr.setAccomplishments(dto.getAccomplishments());
        if (dto.getAreasOfImprovement() != null)
            pr.setAreasOfImprovement(dto.getAreasOfImprovement());
        if (dto.getSelfRating() != null)
            pr.setSelfRating(dto.getSelfRating());
    }
}
