package com.rev.app.mapper;

import com.rev.app.dto.LeaveApplicationDTO;
import com.rev.app.entity.LeaveApplication;
import org.springframework.stereotype.Component;

@Component
public class LeaveApplicationMapper {

    /**
     * Maps a LeaveApplication entity to LeaveApplicationDTO.
     */
    public LeaveApplicationDTO toDto(LeaveApplication la) {
        if (la == null)
            return null;

        LeaveApplicationDTO dto = new LeaveApplicationDTO();
        dto.setLeaveId(la.getLeaveId());

        if (la.getEmployee() != null) {
            dto.setEmployeeId(la.getEmployee().getEmployeeId());
            dto.setEmployeeName(la.getEmployee().getName());
            dto.setEmployeeEmail(la.getEmployee().getEmail());
        }
        if (la.getManager() != null) {
            dto.setManagerId(la.getManager().getEmployeeId());
        }

        dto.setLeaveType(la.getLeaveType() != null ? la.getLeaveType().name() : null);
        dto.setFromDate(la.getFromDate());
        dto.setToDate(la.getToDate());
        dto.setReason(la.getReason());
        dto.setStatus(la.getStatus() != null ? la.getStatus().name() : null);
        dto.setManagerComments(la.getManagerComments());
        dto.setAppliedOn(la.getAppliedOn());

        if (la.getFromDate() != null && la.getToDate() != null) {
            dto.setDays((int) (la.getToDate().toEpochDay() - la.getFromDate().toEpochDay() + 1));
        }
        return dto;
    }
}
