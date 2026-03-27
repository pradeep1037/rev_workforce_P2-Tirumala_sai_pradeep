package com.rev.app.repository;

import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveBalance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class LeaveBalanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Test
    public void testFindByEmployeeEmployeeId() {
        // Arrange
        Employee employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("test@rev.com");
        employee.setPassword("pass");
        employee.setRole(Employee.Role.EMPLOYEE);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        employee = entityManager.persist(employee);

        LeaveBalance leaveBalance = new LeaveBalance(employee, 12, 6, 15);
        entityManager.persist(leaveBalance);
        entityManager.flush();

        // Act
        Optional<LeaveBalance> found = leaveBalanceRepository.findByEmployeeEmployeeId(employee.getEmployeeId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(employee.getEmployeeId(), found.get().getEmployee().getEmployeeId());
        assertEquals(Integer.valueOf(12), found.get().getCasualLeave());
    }

    @Test
    public void testFindByEmployeeManagerEmployeeId() {
        // Arrange
        Employee manager = new Employee();
        manager.setName("Manager");
        manager.setEmail("mgr@rev.com");
        manager.setPassword("pass");
        manager.setRole(Employee.Role.MANAGER);
        manager.setStatus(Employee.EmployeeStatus.ACTIVE);
        manager = entityManager.persist(manager);

        Employee employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("test2@rev.com");
        employee.setPassword("pass");
        employee.setRole(Employee.Role.EMPLOYEE);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        employee.setManager(manager);
        employee = entityManager.persist(employee);

        LeaveBalance leaveBalance = new LeaveBalance(employee, 12, 6, 15);
        entityManager.persist(leaveBalance);
        entityManager.flush();

        // Act
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeManagerEmployeeId(manager.getEmployeeId());

        // Assert
        assertNotNull(balances);
        assertEquals(1, balances.size());
        assertEquals(employee.getEmployeeId(), balances.get(0).getEmployee().getEmployeeId());
    }
}
