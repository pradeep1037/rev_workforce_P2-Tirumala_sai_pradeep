package com.rev.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rev.app.dto.LeaveActionRequest;
import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.dto.LeaveRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveBalance;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        request.setLeaveType("CASUAL_LEAVE");
        request.setFromDate(LocalDate.now().plusDays(1));
        request.setToDate(LocalDate.now().plusDays(2));
        request.setReason("Personal");

        LeaveApplicationDTO responseDto = new LeaveApplicationDTO();
        responseDto.setLeaveId(10L);
        responseDto.setStatus("PENDING");

        when(authService.getCurrentEmployee()).thenReturn(employee);
        when(leaveService.applyLeave(eq(1L), any(LeaveRequest.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/leaves/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveId").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(leaveService, times(1)).applyLeave(eq(1L), any(LeaveRequest.class));
    }

    @Test
    public void testGetMyLeaves_ReturnsList() throws Exception {
        // Arrange
        LeaveApplicationDTO dto1 = new LeaveApplicationDTO();
        dto1.setLeaveId(10L);
        LeaveApplicationDTO dto2 = new LeaveApplicationDTO();
        dto2.setLeaveId(11L);

        when(authService.getCurrentEmployee()).thenReturn(employee);
        when(leaveService.getMyLeaves(1L)).thenReturn(Arrays.asList(dto1, dto2));

        // Act & Assert
        mockMvc.perform(get("/api/leaves/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].leaveId").value(10L))
                .andExpect(jsonPath("$[1].leaveId").value(11L));

        verify(leaveService, times(1)).getMyLeaves(1L);
    }

    /**
     * Test: PUT /api/leaves/{id}/action — manager submits APPROVE action.
     * Expects 200 OK and the returned DTO.
     */
    @Test
    public void testProcessLeave_Approve_ReturnsOk() throws Exception {
        LeaveActionRequest actionRequest = new LeaveActionRequest();
        actionRequest.setAction("APPROVE");
        actionRequest.setComments("Approved");

        LeaveApplicationDTO approvedDTO = new LeaveApplicationDTO();
        approvedDTO.setLeaveId(10L);
        approvedDTO.setStatus("APPROVED");

        when(authService.getCurrentEmployee()).thenReturn(employee);
        when(leaveService.processLeave(eq(10L), any(LeaveActionRequest.class), eq(employee)))
                .thenReturn(approvedDTO);

        mockMvc.perform(put("/api/leaves/10/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveId").value(10L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(leaveService, times(1)).processLeave(eq(10L), any(LeaveActionRequest.class), eq(employee));
    }

    /**
     * Test: DELETE /api/leaves/{id}/cancel — employee cancels their own pending
     * leave.
     * Expects 200 OK with a confirmation message.
     */
    @Test
    public void testCancelLeave_ReturnsOk() throws Exception {
        when(authService.getCurrentEmployee()).thenReturn(employee);
        doNothing().when(leaveService).cancelLeave(10L, 1L);

        mockMvc.perform(delete("/api/leaves/10/cancel"))
                .andExpect(status().isOk());

        verify(leaveService, times(1)).cancelLeave(10L, 1L);
    }
}
