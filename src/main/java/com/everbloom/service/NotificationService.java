package com.everbloom.service;

import com.everbloom.model.Notification;
import com.everbloom.repository.NotificationRepository;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createOrderStatusNotification(long orderId, String status, String orderNumber) throws SQLException {
        if (orderId <= 0 || status == null || status.isBlank() || orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("Order status notification is invalid.");
        }
        notificationRepository.create(orderId, status, "Order " + orderNumber + " is now " + status + ".");
    }

    public List<Notification> findRecent(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Notification limit must be positive.");
        }
        return notificationRepository.findRecent(limit);
    }
}
