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
import com.everbloom.composite.ArrangementGroup;
import com.everbloom.composite.BouquetArrangement;
import com.everbloom.composite.EventPackageComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Order createEventPackage(Order order, ArrangementGroup eventPackage) throws SQLException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long orderId = insertOrder(connection, order, eventPackage.getName());
                insertEventComponent(connection, orderId, null, eventPackage);
                connection.commit();
                order.setId(orderId);
                return order;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Optional<ArrangementGroup> findEventPackage(long orderId) throws SQLException {
        String sql = "SELECT arrangement_id, parent_arrangement_id, name_snapshot, occasion_snapshot, "
                + "arrangement_total_snapshot FROM arrangements WHERE order_id = ? "
                + "AND occasion_snapshot IN ('EVENT_GROUP', 'EVENT_ARRANGEMENT') ORDER BY arrangement_id";
        Map<Long, EventPackageComponent> components = new HashMap<>();
        Map<Long, Long> parents = new HashMap<>();
        Long rootId = null;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long arrangementId = resultSet.getLong("arrangement_id");
                    String name = resultSet.getString("name_snapshot");
                    String type = resultSet.getString("occasion_snapshot");
                    EventPackageComponent component;
                    if ("EVENT_GROUP".equals(type)) {
                        component = new ArrangementGroup(name);
                    } else {
                        component = new BouquetArrangement(name, resultSet.getLong("arrangement_total_snapshot"));
                    }
                    components.put(arrangementId, component);
                    long parentId = resultSet.getLong("parent_arrangement_id");
                    if (resultSet.wasNull()) {
                        rootId = arrangementId;
                    } else {
                        parents.put(arrangementId, parentId);
                    }
                }
            }
        }
        for (Map.Entry<Long, Long> entry : parents.entrySet()) {
            EventPackageComponent parent = components.get(entry.getValue());
            if (parent instanceof ArrangementGroup group) {
                group.add(components.get(entry.getKey()));
            }
        }
        if (rootId == null || !(components.get(rootId) instanceof ArrangementGroup root)) {
            return Optional.empty();
        }
        return Optional.of(root);
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
        return insertOrder(connection, order, order.getBouquet().getOccasion());
    }

    private long insertOrder(Connection connection, Order order, String occasion) throws SQLException {
        String sql = "INSERT INTO orders (order_number, customer_id, occasion_snapshot, fulfillment_type, "
                + "delivery_address_snapshot, status, pricing_policy_snapshot, subtotal_snapshot, "
                + "discount_snapshot, total_snapshot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, order.getOrderNumber());
            statement.setLong(2, order.getCustomer().getId());
            statement.setString(3, occasion);
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

    private long insertEventComponent(Connection connection, long orderId, Long parentId,
                                      EventPackageComponent component) throws SQLException {
        boolean group = component instanceof ArrangementGroup;
        String sql = "INSERT INTO arrangements (order_id, parent_arrangement_id, name_snapshot, "
                + "occasion_snapshot, base_price_snapshot, extras_price_snapshot, arrangement_total_snapshot) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?)";
        long storedTotal = group ? 0 : component.getTotalPrice();
        long arrangementId;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, orderId);
            if (parentId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setLong(2, parentId);
            }
            statement.setString(3, component.getName());
            statement.setString(4, group ? "EVENT_GROUP" : "EVENT_ARRANGEMENT");
            statement.setLong(5, storedTotal);
            statement.setLong(6, storedTotal);
            statement.executeUpdate();
            arrangementId = generatedId(statement);
        }
        if (component instanceof ArrangementGroup arrangementGroup) {
            for (EventPackageComponent child : arrangementGroup.getChildren()) {
                insertEventComponent(connection, orderId, arrangementId, child);
            }
        }
        return arrangementId;
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
