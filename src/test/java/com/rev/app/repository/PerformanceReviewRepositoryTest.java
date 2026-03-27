package com.rev.app.repository;

import com.rev.app.entity.Employee;
import com.rev.app.entity.PerformanceReview;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PerformanceReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PerformanceReviewRepository repository;

    private Employee employee;

    @Before
    public void setUp() {
        employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("test@revworkforce.com");
        employee.setPassword("password");
        employee.setRole(Employee.Role.EMPLOYEE);
        entityManager.persistAndFlush(employee);
    }

    @Test
    public void whenFindByEmployeeEmployeeIdAndYear_thenReturnReview() {
        // given
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setYear(2024);
        review.setStatus(PerformanceReview.ReviewStatus.DRAFT);
        entityManager.persistAndFlush(review);

        // when
        Optional<PerformanceReview> found = repository.findByEmployeeEmployeeIdAndYear(employee.getEmployeeId(), 2024);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getYear()).isEqualTo(2024);
        assertThat(found.get().getEmployee().getEmployeeId()).isEqualTo(employee.getEmployeeId());
    }
}
