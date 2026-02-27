package com.rev.app.mapper;

import com.rev.app.dto.NotificationDTO;
import com.rev.app.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    /**
     * Maps a Notification entity to NotificationDTO.
     */
    public NotificationDTO toDto(Notification n) {
        if (n == null)
            return null;

        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(n.getNotificationId());

        if (n.getEmployee() != null) {
            dto.setEmployeeId(n.getEmployee().getEmployeeId());
        }

        dto.setMessage(n.getMessage());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setType(n.getType() != null ? n.getType().name() : null);
        dto.setRefId(n.getRefId());

        return dto;
    }

    /**
     * Maps a list of Notification entities to a list of NotificationDTOs.
     */
    public List<NotificationDTO> toDtoList(List<Notification> notifications) {
        if (notifications == null)
            return List.of();
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
