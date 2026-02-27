package com.rev.app.repository;

import com.rev.app.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPerformedByEmployeeIdOrderByActionTimeDesc(Long employeeId);

    List<AuditLog> findAllByOrderByActionTimeDesc();
}
