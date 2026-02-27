package com.rev.app.mapper;

import com.rev.app.dto.AuditLogDTO;
import com.rev.app.entity.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditLogMapper {

    /**
     * Maps an AuditLog entity to AuditLogDTO.
     */
    public AuditLogDTO toDto(AuditLog log) {
        if (log == null)
            return null;

        AuditLogDTO dto = new AuditLogDTO();
        dto.setLogId(log.getLogId());

        if (log.getPerformedBy() != null) {
            dto.setPerformedById(log.getPerformedBy().getEmployeeId());
            dto.setPerformedByName(log.getPerformedBy().getName());
            dto.setPerformedByEmail(log.getPerformedBy().getEmail());
        }

        dto.setAction(log.getAction());
        dto.setActionTime(log.getActionTime());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());

        return dto;
    }

    /**
     * Maps a list of AuditLog entities to a list of AuditLogDTOs.
     */
    public List<AuditLogDTO> toDtoList(List<AuditLog> logs) {
        if (logs == null)
            return List.of();
        return logs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
