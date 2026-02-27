package com.rev.app.service;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.entity.Employee;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EmployeeMapper;
import com.rev.app.repository.DepartmentRepository;
import com.rev.app.repository.DesignationRepository;
import com.rev.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private EmployeeDTO mockDTO;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee();
        mockEmployee.setEmployeeId(1L);
        mockEmployee.setName("John Doe");
        mockEmployee.setStatus(Employee.EmployeeStatus.ACTIVE);

        mockDTO = new EmployeeDTO();
        mockDTO.setEmployeeId(1L);
        mockDTO.setName("John Doe");
    }

    @Test
    void testGetAllEmployees() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(mockEmployee));
        when(employeeMapper.toDto(any())).thenReturn(mockDTO);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toDto(mockEmployee)).thenReturn(mockDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void testSearchEmployees() {
        // Arrange
        when(employeeRepository.searchEmployees("John")).thenReturn(Arrays.asList(mockEmployee));
        when(employeeMapper.toDto(any())).thenReturn(mockDTO);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployees("John");

        // Assert
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).searchEmployees("John");
    }
}
