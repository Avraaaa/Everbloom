package com.everbloom.service;

import com.everbloom.model.DashboardOrder;
import com.everbloom.model.Notification;
import com.everbloom.repository.DashboardRepository;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final NotificationService notificationService;

    public DashboardService(DashboardRepository dashboardRepository, NotificationService notificationService) {
        this.dashboardRepository = dashboardRepository;
        this.notificationService = notificationService;
    }

    public long getTodayOrderCount() throws SQLException { return dashboardRepository.countTodayOrders(); }
    public long getReadyOrderCount() throws SQLException { return dashboardRepository.countReadyOrders(); }
    public long getTodayRevenue() throws SQLException { return dashboardRepository.getTodayRevenue(); }
    public long getCustomerCount() throws SQLException { return dashboardRepository.countCustomers(); }
    public List<DashboardOrder> findRecentOrders() throws SQLException { return dashboardRepository.findRecentOrders(6); }
    public List<Notification> findRecentNotifications() throws SQLException { return notificationService.findRecent(5); }
}
