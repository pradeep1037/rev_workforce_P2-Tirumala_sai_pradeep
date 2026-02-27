package com.rev.app.service;

import com.rev.app.entity.Employee;
import com.rev.app.entity.Notification;
import com.rev.app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void send(Employee employee, String message, Notification.NotificationType type, Long refId) {
        Notification n = new Notification();
        n.setEmployee(employee);
        n.setMessage(message);
        n.setType(type);
        n.setRefId(refId);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    public List<Notification> getNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeEmployeeIdAndIsReadFalse(employeeId);
    }

    @Transactional
    public void markAllRead(Long employeeId) {
        List<Notification> unread = notificationRepository.findByEmployeeEmployeeIdAndIsReadFalse(employeeId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
