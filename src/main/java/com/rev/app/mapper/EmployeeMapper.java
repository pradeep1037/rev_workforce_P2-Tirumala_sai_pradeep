package com.rev.app.mapper;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    /**
     * Maps an Employee entity to EmployeeDTO.
     */
    public EmployeeDTO toDto(Employee e) {
        if (e == null)
            return null;

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(e.getEmployeeId());
        dto.setName(e.getName());
        dto.setEmail(e.getEmail());
        dto.setRole(e.getRole() != null ? e.getRole().name() : null);
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        dto.setPhone(e.getPhone());
        dto.setAddress(e.getAddress());
        dto.setEmergencyContact(e.getEmergencyContact());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setSalary(e.getSalary());

        if (e.getDepartment() != null) {
            dto.setDepartmentId(e.getDepartment().getDeptId());
            dto.setDepartmentName(e.getDepartment().getDeptName());
        }
        if (e.getDesignation() != null) {
            dto.setDesignationId(e.getDesignation().getDesigId());
            dto.setDesignationName(e.getDesignation().getDesigName());
        }
        if (e.getManager() != null) {
            dto.setManagerId(e.getManager().getEmployeeId());
            dto.setManagerName(e.getManager().getName());
            dto.setManagerEmail(e.getManager().getEmail());
        }
        return dto;
    }

    /**
     * Applies DTO fields onto an existing Employee entity (partial update).
     * Password, role, status are intentionally excluded — manage those separately.
     */
    public void updateEntityFromDto(EmployeeDTO dto, Employee e) {
        if (dto == null || e == null)
            return;
        if (dto.getName() != null)
            e.setName(dto.getName());
        if (dto.getPhone() != null)
            e.setPhone(dto.getPhone());
        if (dto.getAddress() != null)
            e.setAddress(dto.getAddress());
        if (dto.getEmergencyContact() != null)
            e.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getJoiningDate() != null)
            e.setJoiningDate(dto.getJoiningDate());
        if (dto.getSalary() != null)
            e.setSalary(dto.getSalary());
    }
}
