package com.rev.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.Employee;
import com.rev.app.service.IAuthService;
import com.rev.app.service.ILeaveService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class LeaveControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthService authService;

    @Mock
    private ILeaveService leaveService;

    @InjectMocks
    private LeaveController leaveController;

    private ObjectMapper objectMapper;
    private Employee employee;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setName("Test User");
    }

    @Test
    public void testApplyLeave_Success() throws Exception {
        // Arrange
        LeaveRequest request = new LeaveRequest();
        request.setLeaveType("CASUAL");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReason("Personal");

        LeaveApplicationDTO responseDto = new LeaveApplicationDTO();
        responseDto.setId(10L);
        responseDto.setStatus("PENDING");

        when(authService.getCurrentEmployee()).thenReturn(employee);
        when(leaveService.applyLeave(eq(1L), any(LeaveRequest.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/leaves/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(leaveService, times(1)).applyLeave(eq(1L), any(LeaveRequest.class));
    }

    @Test
    public void testGetMyLeaves_ReturnsList() throws Exception {
        // Arrange
        LeaveApplicationDTO dto1 = new LeaveApplicationDTO();
        dto1.setId(10L);
        LeaveApplicationDTO dto2 = new LeaveApplicationDTO();
        dto2.setId(11L);

        when(authService.getCurrentEmployee()).thenReturn(employee);
        when(leaveService.getMyLeaves(1L)).thenReturn(Arrays.asList(dto1, dto2));

        // Act & Assert
        mockMvc.perform(get("/api/leaves/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[1].id").value(11L));

        verify(leaveService, times(1)).getMyLeaves(1L);
    }
}
