package com.rev.app.repository;

import com.rev.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<Notification> findByEmployeeEmployeeIdAndIsReadFalse(Long employeeId);

    long countByEmployeeEmployeeIdAndIsReadFalse(Long employeeId);
}
