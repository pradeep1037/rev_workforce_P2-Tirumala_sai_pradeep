package com.rev.app.mapper;

import com.rev.app.dto.AnnouncementDTO;
import com.rev.app.entity.Announcement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnnouncementMapper {

    /**
     * Maps an Announcement entity to AnnouncementDTO.
     */
    public AnnouncementDTO toDto(Announcement a) {
        if (a == null)
            return null;

        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setAnnouncementId(a.getAnnouncementId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setIsActive(a.getIsActive());

        if (a.getCreatedBy() != null) {
            dto.setCreatedById(a.getCreatedBy().getEmployeeId());
            dto.setCreatedByName(a.getCreatedBy().getName());
        }

        return dto;
    }

    /**
     * Maps a list of Announcement entities to a list of AnnouncementDTOs.
     */
    public List<AnnouncementDTO> toDtoList(List<Announcement> announcements) {
        if (announcements == null)
            return List.of();
        return announcements.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Applies update fields from an AnnouncementDTO onto an existing Announcement
     * entity.
     */
    public void updateEntityFromDto(AnnouncementDTO dto, Announcement a) {
        if (dto == null || a == null)
            return;
        if (dto.getTitle() != null)
            a.setTitle(dto.getTitle());
        if (dto.getContent() != null)
            a.setContent(dto.getContent());
        if (dto.getIsActive() != null)
            a.setIsActive(dto.getIsActive());
    }
}
