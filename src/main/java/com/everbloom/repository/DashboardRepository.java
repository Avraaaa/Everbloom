package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.DashboardOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {

    private final DatabaseConnection databaseConnection;

    public DashboardRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public long countTodayOrders() throws SQLException { return readCount("SELECT COUNT(*) FROM orders WHERE date(placed_at) = date('now')"); }
    public long countReadyOrders() throws SQLException { return readCount("SELECT COUNT(*) FROM orders WHERE status = 'READY'"); }
    public long getTodayRevenue() throws SQLException { return readCount("SELECT COALESCE(SUM(total_snapshot), 0) FROM orders WHERE date(placed_at) = date('now')"); }
    public long countCustomers() throws SQLException { return readCount("SELECT COUNT(*) FROM customers"); }

    public List<DashboardOrder> findRecentOrders(int limit) throws SQLException {
        String sql = "SELECT o.order_number, c.full_name, o.occasion_snapshot, o.status, o.total_snapshot "
                + "FROM orders o JOIN customers c ON o.customer_id = c.customer_id "
                + "ORDER BY o.placed_at DESC, o.order_id DESC LIMIT ?";
        List<DashboardOrder> orders = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(new DashboardOrder(resultSet.getString("order_number"), resultSet.getString("full_name"),
                            resultSet.getString("occasion_snapshot"), resultSet.getString("status"), resultSet.getLong("total_snapshot")));
                }
            }
        }
        return orders;
    }

    private long readCount(String sql) throws SQLException {
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
