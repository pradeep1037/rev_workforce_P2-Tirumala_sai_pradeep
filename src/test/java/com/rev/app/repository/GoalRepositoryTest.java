package com.rev.app.repository;

import com.rev.app.entity.Employee;
import com.rev.app.entity.Goal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class GoalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GoalRepository repository;

    private Employee manager;
    private Employee employee1;
    private Employee employee2;

    @Before
    public void setUp() {
        manager = new Employee();
        manager.setName("Manager");
        manager.setEmail("manager@revworkforce.com");
        manager.setPassword("password");
        manager.setRole(Employee.Role.MANAGER);
        entityManager.persist(manager);

        employee1 = new Employee();
        employee1.setName("Employee 1");
        employee1.setEmail("e1@revworkforce.com");
        employee1.setPassword("password");
        employee1.setRole(Employee.Role.EMPLOYEE);
        employee1.setManager(manager);
        entityManager.persist(employee1);

        employee2 = new Employee();
        employee2.setName("Employee 2");
        employee2.setEmail("e2@revworkforce.com");
        employee2.setPassword("password");
        employee2.setRole(Employee.Role.EMPLOYEE);
        entityManager.persist(employee2);

        entityManager.flush();
    }

    @Test
    public void whenFindByEmployeeEmployeeId_thenReturnGoalsList() {
        // given
        Goal goal1 = new Goal();
        goal1.setEmployee(employee1);
        goal1.setGoalDescription("Goal 1");
        goal1.setPriority(Goal.Priority.HIGH);
        goal1.setStatus(Goal.GoalStatus.NOT_STARTED);
        entityManager.persist(goal1);

        Goal goal2 = new Goal();
        goal2.setEmployee(employee1);
        goal2.setGoalDescription("Goal 2");
        goal2.setPriority(Goal.Priority.MEDIUM);
        goal2.setStatus(Goal.GoalStatus.IN_PROGRESS);
        entityManager.persist(goal2);

        entityManager.flush();

        // when
        List<Goal> found = repository.findByEmployeeEmployeeId(employee1.getEmployeeId());

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Goal::getGoalDescription).containsExactlyInAnyOrder("Goal 1", "Goal 2");
    }

    @Test
    public void whenFindByEmployeeManagerEmployeeId_thenReturnTeamGoals() {
        // given
        Goal goal1 = new Goal();
        goal1.setEmployee(employee1); // employee1 is under manager
        goal1.setGoalDescription("Goal 1");
        goal1.setPriority(Goal.Priority.HIGH);
        goal1.setStatus(Goal.GoalStatus.NOT_STARTED);
        entityManager.persist(goal1);

        Goal goal2 = new Goal();
        goal2.setEmployee(employee2); // employee2 is NOT under manager
        goal2.setGoalDescription("Goal 2");
        goal2.setPriority(Goal.Priority.MEDIUM);
        goal2.setStatus(Goal.GoalStatus.IN_PROGRESS);
        entityManager.persist(goal2);

        entityManager.flush();

        // when
        List<Goal> found = repository.findByEmployeeManagerEmployeeId(manager.getEmployeeId());

        // then
        assertThat(found).hasSize(1); // Should only get employee1's goal
        assertThat(found.get(0).getEmployee().getName()).isEqualTo("Employee 1");
    }
}
