package com.rev.app.service;

import com.rev.app.entity.AuditLog;
import com.rev.app.entity.Employee;
import com.rev.app.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Employee performedBy, String action, String entityType, Long entityId) {
        AuditLog log = new AuditLog();
        log.setPerformedBy(performedBy);
        log.setAction(action);
        log.setActionTime(LocalDateTime.now());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByActionTimeDesc();
    }
}
