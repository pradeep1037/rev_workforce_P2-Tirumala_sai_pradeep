package com.rev.app.service;

import com.rev.app.entity.Employee;
import com.rev.app.entity.Notification;

import java.util.List;

public interface INotificationService {

    void send(Employee employee, String message, Notification.NotificationType type, Long refId);

    List<Notification> getNotifications(Long employeeId);

    long getUnreadCount(Long employeeId);

    void markAllRead(Long employeeId);

    void markRead(Long notificationId);
}
