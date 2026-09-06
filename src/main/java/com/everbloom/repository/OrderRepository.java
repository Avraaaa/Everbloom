package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.Order;
import com.everbloom.model.Extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.everbloom.model.Customer;

public class OrderRepository {

    private final DatabaseConnection databaseConnection;

    public OrderRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Order create(Order order) throws SQLException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long orderId = insertOrder(connection, order);
                long arrangementId = insertArrangement(connection, order, orderId);
                insertFlowers(connection, order, arrangementId);
                insertExtras(connection, order, arrangementId);
                connection.commit();
                order.setId(orderId);
                return order;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<Order> findAll() throws SQLException {
        String sql = "SELECT o.*, c.full_name, c.phone FROM orders o JOIN customers c ON o.customer_id = c.customer_id ORDER BY o.placed_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Customer customer = new Customer(resultSet.getLong("customer_id"), resultSet.getString("full_name"), resultSet.getString("phone"), null, null);
                orders.add(new Order(resultSet.getLong("order_id"), resultSet.getString("order_number"), customer, null, List.of(), resultSet.getString("fulfillment_type"), resultSet.getString("delivery_address_snapshot"), resultSet.getString("pricing_policy_snapshot"), resultSet.getLong("subtotal_snapshot"), resultSet.getLong("discount_snapshot"), resultSet.getLong("total_snapshot"), resultSet.getString("status"), null));
            }
        }
        return orders;
    }

    public void updateStatus(Order order) throws SQLException {
        String sql = "UPDATE orders SET status = ?, status_updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getStatus());
            statement.setLong(2, order.getId());
            statement.executeUpdate();
        }
    }

    private long insertOrder(Connection connection, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_number, customer_id, occasion_snapshot, fulfillment_type, "
                + "delivery_address_snapshot, status, pricing_policy_snapshot, subtotal_snapshot, "
                + "discount_snapshot, total_snapshot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, order.getOrderNumber());
            statement.setLong(2, order.getCustomer().getId());
            statement.setString(3, order.getBouquet().getOccasion());
            statement.setString(4, order.getFulfillmentType());
            statement.setString(5, order.getDeliveryAddress());
            statement.setString(6, order.getStatus());
            statement.setString(7, order.getPricingPolicy());
            statement.setLong(8, order.getSubtotal());
            statement.setLong(9, order.getDiscount());
            statement.setLong(10, order.getTotal());
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    private long insertArrangement(Connection connection, Order order, long orderId) throws SQLException {
        String sql = "INSERT INTO arrangements (order_id, name_snapshot, occasion_snapshot, wrapping_style_snapshot, "
                + "message_text_snapshot, base_price_snapshot, extras_price_snapshot, arrangement_total_snapshot) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long basePrice = order.getBouquet().getFlowerSubtotal();
        long extrasPrice = order.getSubtotal() - basePrice;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, orderId);
            statement.setString(2, order.getBouquet().getOccasion() + " bouquet");
            statement.setString(3, order.getBouquet().getOccasion());
            statement.setString(4, order.getBouquet().getWrappingStyle());
            statement.setString(5, order.getBouquet().getMessage());
            statement.setLong(6, basePrice);
            statement.setLong(7, extrasPrice);
            statement.setLong(8, order.getSubtotal());
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    private void insertFlowers(Connection connection, Order order, long arrangementId) throws SQLException {
        String sql = "INSERT INTO arrangement_flowers (arrangement_id, flower_id, flower_name_snapshot, "
                + "unit_price_snapshot, quantity, line_total_snapshot) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (BouquetFlower bouquetFlower : order.getBouquet().getFlowers()) {
                statement.setLong(1, arrangementId);
                statement.setLong(2, bouquetFlower.getFlower().getId());
                statement.setString(3, bouquetFlower.getFlower().getName());
                statement.setLong(4, bouquetFlower.getFlower().getUnitPrice());
                statement.setInt(5, bouquetFlower.getQuantity());
                statement.setLong(6, bouquetFlower.getLineTotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertExtras(Connection connection, Order order, long arrangementId) throws SQLException {
        String sql = "INSERT INTO arrangement_extras (arrangement_id, extra_id, extra_name_snapshot, "
                + "unit_price_snapshot, quantity, line_total_snapshot) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Extra extra : order.getExtras()) {
                statement.setLong(1, arrangementId);
                statement.setLong(2, extra.getId());
                statement.setString(3, extra.getName());
                statement.setLong(4, extra.getUnitPrice());
                statement.setInt(5, 1);
                statement.setLong(6, extra.getUnitPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private long generatedId(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }
        throw new SQLException("Unable to create order record.");
    }
}
