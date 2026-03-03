package com.rev.app.service;

import com.rev.app.dto.PerformanceReviewDTO;
import com.rev.app.dto.PerformanceReviewRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.PerformanceReview;
import com.rev.app.mapper.PerformanceMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.GoalRepository;
import com.rev.app.repository.PerformanceReviewRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private PerformanceMapper performanceMapper;

    @InjectMocks
    private PerformanceService performanceService;

    private Employee employee;
    private PerformanceReview review;
    private PerformanceReviewDTO reviewDTO;

    @Before
    public void setUp() {
        employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setName("John Doe");

        review = new PerformanceReview();
        review.setReviewId(10L);
        review.setEmployee(employee);
        review.setStatus(PerformanceReview.ReviewStatus.DRAFT);

        reviewDTO = new PerformanceReviewDTO();
        reviewDTO.setId(10L);
        reviewDTO.setStatus("DRAFT");
    }

    @Test
    public void testCreateOrUpdateReview_NewReview() {
        // Arrange
        PerformanceReviewRequest req = new PerformanceReviewRequest();
        req.setReviewCycle("Q1 2024");
        req.setSelfRating(4);

        when(reviewRepository.findByEmployeeEmployeeIdAndReviewCycle(1L, "Q1 2024"))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        when(performanceMapper.toReviewDto(review)).thenReturn(reviewDTO);

        // Act
        PerformanceReviewDTO result = performanceService.createOrUpdateReview(1L, req, employee);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(10L), result.getId());
        verify(reviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    public void testSubmitReview() {
        // Arrange
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        PerformanceReviewDTO submittedDTO = new PerformanceReviewDTO();
        submittedDTO.setId(10L);
        submittedDTO.setStatus("SUBMITTED");
        when(performanceMapper.toReviewDto(review)).thenReturn(submittedDTO);

        // Act
        PerformanceReviewDTO result = performanceService.submitReview(10L, employee);

        // Assert
        assertEquals("SUBMITTED", result.getStatus());
        assertEquals(PerformanceReview.ReviewStatus.SUBMITTED, review.getStatus());
        verify(reviewRepository, times(1)).save(review);
    }
}
