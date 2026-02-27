package com.rev.app.config;

import com.rev.app.entity.*;
import com.rev.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Seeds the database with initial data on first run.
 * Idempotent - checks before inserting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Database already seeded. Skipping initialization.");
            return;
        }

        log.info("Seeding database with initial data...");

        // Departments
        Department hrDept = new Department(null, "Human Resources");
        Department itDept = new Department(null, "Information Technology");
        Department financeDept = new Department(null, "Finance");
        departmentRepository.save(hrDept);
        departmentRepository.save(itDept);
        departmentRepository.save(financeDept);

        // Designations
        Designation adminDesig = new Designation(null, "System Administrator");
        Designation managerDesig = new Designation(null, "Engineering Manager");
        Designation developerDesig = new Designation(null, "Software Developer");
        Designation hrManagerDesig = new Designation(null, "HR Manager");
        designationRepository.save(adminDesig);
        designationRepository.save(managerDesig);
        designationRepository.save(developerDesig);
        designationRepository.save(hrManagerDesig);

        // Admin Employee
        Employee admin = new Employee();
        admin.setName("Admin User");
        admin.setEmail("admin@revworkforce.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Employee.Role.ADMIN);
        admin.setDepartment(hrDept);
        admin.setDesignation(adminDesig);
        admin.setStatus(Employee.EmployeeStatus.ACTIVE);
        admin.setJoiningDate(LocalDate.of(2020, 1, 1));
        admin.setSalary(100000.0);
        admin.setPhone("9000000001");
        employeeRepository.save(admin);
        leaveBalanceRepository.save(new LeaveBalance(admin, 12, 6, 15));
        log.info("Admin created: admin@revworkforce.com / Admin@123");

        // Manager Employee
        Employee manager = new Employee();
        manager.setName("RaviKumar Manager");
        manager.setEmail("manager@revworkforce.com");
        manager.setPassword(passwordEncoder.encode("Manager@123"));
        manager.setRole(Employee.Role.MANAGER);
        manager.setDepartment(itDept);
        manager.setDesignation(managerDesig);
        manager.setStatus(Employee.EmployeeStatus.ACTIVE);
        manager.setManager(admin);
        manager.setJoiningDate(LocalDate.of(2021, 3, 15));
        manager.setSalary(80000.0);
        manager.setPhone("9000000002");
        employeeRepository.save(manager);
        leaveBalanceRepository.save(new LeaveBalance(manager, 12, 6, 15));
        log.info("Manager created: manager@revworkforce.com / Manager@123");

        // Employee
        Employee emp = new Employee();
        emp.setName("Employee1");
        emp.setEmail("employee@revworkforce.com");
        emp.setPassword(passwordEncoder.encode("Employee@123"));
        emp.setRole(Employee.Role.EMPLOYEE);
        emp.setDepartment(itDept);
        emp.setDesignation(developerDesig);
        emp.setStatus(Employee.EmployeeStatus.ACTIVE);
        emp.setManager(manager);
        emp.setJoiningDate(LocalDate.of(2022, 6, 1));
        emp.setSalary(60000.0);
        emp.setPhone("9000000003");
        employeeRepository.save(emp);
        leaveBalanceRepository.save(new LeaveBalance(emp, 12, 6, 15));
        log.info("Employee created: employee@revworkforce.com / Employee@123");

        log.info("Database seeding completed successfully!");
    }
}
