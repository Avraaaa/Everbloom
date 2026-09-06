package com.everbloom.observer;

import com.everbloom.model.Order;
import com.everbloom.service.NotificationService;

import java.sql.SQLException;

public class NotificationObserver implements OrderObserver {

    private final NotificationService notificationService;

    public NotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onOrderStatusChanged(Order order) throws SQLException {
        notificationService.createOrderStatusNotification(order.getId(), order.getStatus(), order.getOrderNumber());
    }
}
