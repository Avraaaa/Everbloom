package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final DatabaseConnection databaseConnection;

    public NotificationRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void create(long orderId, String status, String message) throws SQLException {
        String sql = "INSERT INTO notifications (order_id, status_snapshot, message) VALUES (?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setString(2, status);
            statement.setString(3, message);
            statement.executeUpdate();
        }
    }

    public List<Notification> findRecent(int limit) throws SQLException {
        String sql = "SELECT n.notification_id, n.order_id, o.order_number, n.status_snapshot, n.message, n.created_at "
                + "FROM notifications n JOIN orders o ON n.order_id = o.order_id "
                + "ORDER BY n.created_at DESC, n.notification_id DESC LIMIT ?";
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(new Notification(
                            resultSet.getLong("notification_id"),
                            resultSet.getLong("order_id"),
                            resultSet.getString("order_number"),
                            resultSet.getString("status_snapshot"),
                            resultSet.getString("message"),
                            resultSet.getString("created_at")
                    ));
                }
            }
        }
        return notifications;
    }
}
