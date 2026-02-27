package com.rev.app.repository;

import com.rev.app.entity.PerformanceReview;
import com.rev.app.entity.PerformanceReview.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeEmployeeId(Long employeeId);

    List<PerformanceReview> findByEmployeeManagerEmployeeId(Long managerId);

    Optional<PerformanceReview> findByEmployeeEmployeeIdAndYear(Long employeeId, Integer year);

    List<PerformanceReview> findByEmployeeManagerEmployeeIdAndStatus(Long managerId, ReviewStatus status);
}
