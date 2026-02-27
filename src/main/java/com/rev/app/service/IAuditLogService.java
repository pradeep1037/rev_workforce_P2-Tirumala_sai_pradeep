package com.rev.app.service;

import com.rev.app.entity.AuditLog;
import com.rev.app.entity.Employee;

import java.util.List;

public interface IAuditLogService {

    void log(Employee performedBy, String action, String entityType, Long entityId);

    List<AuditLog> getAllLogs();
}
