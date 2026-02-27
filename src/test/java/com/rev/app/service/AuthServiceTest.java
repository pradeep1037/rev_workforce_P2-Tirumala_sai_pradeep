package com.rev.app.service;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.dto.RegisterRequest;
import com.rev.app.entity.Employee;
import com.rev.app.exception.BadRequestException;
import com.rev.app.mapper.EmployeeMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.LeaveBalanceRepository;
import com.rev.app.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @InjectMocks
    private AuthService authService;

    private Employee mockEmployee;
    private RegisterRequest mockRegisterReq;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee();
        mockEmployee.setEmployeeId(1L);
        mockEmployee.setName("Test User");
        mockEmployee.setEmail("test@revworkforce.com");
        mockEmployee.setRole(Employee.Role.EMPLOYEE);

        mockRegisterReq = new RegisterRequest();
        mockRegisterReq.setName("New User");
        mockRegisterReq.setEmail("new@revworkforce.com");
        mockRegisterReq.setPassword("Password@123");
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test@revworkforce.com");
        loginReq.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("mock-jwt-token");
        when(employeeRepository.findByEmail("test@revworkforce.com")).thenReturn(Optional.of(mockEmployee));

        // Act
        LoginResponse response = authService.login(loginReq);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(mockEmployee.getEmail(), response.getEmail());
        assertEquals("EMPLOYEE", response.getRole());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(employeeRepository.findByEmail(mockRegisterReq.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        Employee savedEmp = new Employee();
        savedEmp.setEmployeeId(2L);
        savedEmp.setName(mockRegisterReq.getName());
        savedEmp.setEmail(mockRegisterReq.getEmail());
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmp);

        EmployeeDTO mockDto = new EmployeeDTO();
        mockDto.setEmail(mockRegisterReq.getEmail());
        mockDto.setName(mockRegisterReq.getName());
        when(employeeMapper.toDto(savedEmp)).thenReturn(mockDto);

        // Act
        EmployeeDTO result = authService.register(mockRegisterReq);

        // Assert
        assertNotNull(result);
        assertEquals(mockRegisterReq.getEmail(), result.getEmail());
        verify(leaveBalanceRepository, times(1)).save(any());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(employeeRepository.findByEmail(mockRegisterReq.getEmail())).thenReturn(Optional.of(mockEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.register(mockRegisterReq));
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}
