package com.rev.app.security;

import com.rev.app.entity.Employee;
import com.rev.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee();
        mockEmployee.setEmployeeId(1L);
        mockEmployee.setEmail("test@revworkforce.com");
        mockEmployee.setPassword("hashedPassword123");
        mockEmployee.setRole(Employee.Role.EMPLOYEE);
        mockEmployee.setStatus(Employee.EmployeeStatus.ACTIVE);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(employeeRepository.findByEmail("test@revworkforce.com")).thenReturn(Optional.of(mockEmployee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@revworkforce.com");

        assertNotNull(userDetails);
        assertEquals("test@revworkforce.com", userDetails.getUsername());
        assertEquals("hashedPassword123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));

        verify(employeeRepository, times(1)).findByEmail("test@revworkforce.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(employeeRepository.findByEmail("unknown@revworkforce.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown@revworkforce.com"));

        verify(employeeRepository, times(1)).findByEmail("unknown@revworkforce.com");
    }

    @Test
    void testLoadUserByUsername_InactiveUser() {
        mockEmployee.setStatus(Employee.EmployeeStatus.INACTIVE);
        when(employeeRepository.findByEmail("test@revworkforce.com")).thenReturn(Optional.of(mockEmployee));

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("test@revworkforce.com"));

        verify(employeeRepository, times(1)).findByEmail("test@revworkforce.com");
    }

    @Test
    void testLoadUserByUsername_AdminRole() {
        mockEmployee.setRole(Employee.Role.ADMIN);
        when(employeeRepository.findByEmail("admin@revworkforce.com")).thenReturn(Optional.of(mockEmployee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@revworkforce.com");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
