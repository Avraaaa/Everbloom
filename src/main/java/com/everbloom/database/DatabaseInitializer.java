package com.everbloom.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private final DatabaseConnection databaseConnection;

    public DatabaseInitializer(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void initialize() throws SQLException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                createTables(connection);
                seedData(connection);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void createTables(Connection connection) throws SQLException {
        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY,
                    full_name TEXT NOT NULL CHECK (length(trim(full_name)) > 0),
                    phone TEXT NOT NULL UNIQUE CHECK (length(trim(phone)) > 0),
                    email TEXT CHECK (email IS NULL OR length(trim(email)) > 0),
                    address TEXT CHECK (address IS NULL OR length(trim(address)) > 0),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS flowers (
                    flower_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK (length(trim(name)) > 0),
                    color TEXT,
                    current_unit_price INTEGER NOT NULL CHECK (current_unit_price >= 0),
                    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS extras (
                    extra_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK (length(trim(name)) > 0),
                    description TEXT,
                    current_unit_price INTEGER NOT NULL CHECK (current_unit_price >= 0),
                    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS bouquet_templates (
                    template_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK (length(trim(name)) > 0),
                    occasion TEXT NOT NULL CHECK (length(trim(occasion)) > 0),
                    description TEXT,
                    wrapping_style TEXT,
                    message_text TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS bouquet_template_flowers (
                    template_id INTEGER NOT NULL,
                    flower_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL CHECK (quantity > 0),
                    PRIMARY KEY (template_id, flower_id),
                    FOREIGN KEY (template_id) REFERENCES bouquet_templates(template_id) ON DELETE CASCADE,
                    FOREIGN KEY (flower_id) REFERENCES flowers(flower_id) ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS orders (
                    order_id INTEGER PRIMARY KEY,
                    order_number TEXT NOT NULL UNIQUE CHECK (length(trim(order_number)) > 0),
                    customer_id INTEGER NOT NULL,
                    occasion_snapshot TEXT NOT NULL CHECK (length(trim(occasion_snapshot)) > 0),
                    fulfillment_type TEXT NOT NULL CHECK (fulfillment_type IN ('PICKUP', 'DELIVERY')),
                    delivery_address_snapshot TEXT,
                    status TEXT NOT NULL CHECK (status IN ('ORDERED', 'PREPARING', 'ARRANGING', 'READY', 'DELIVERED')),
                    pricing_policy_snapshot TEXT NOT NULL CHECK (length(trim(pricing_policy_snapshot)) > 0),
                    subtotal_snapshot INTEGER NOT NULL CHECK (subtotal_snapshot >= 0),
                    discount_snapshot INTEGER NOT NULL DEFAULT 0 CHECK (discount_snapshot >= 0 AND discount_snapshot <= subtotal_snapshot),
                    total_snapshot INTEGER NOT NULL CHECK (total_snapshot >= 0 AND total_snapshot = subtotal_snapshot - discount_snapshot),
                    placed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status_updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CHECK (
                        (fulfillment_type = 'DELIVERY' AND length(trim(delivery_address_snapshot)) > 0)
                        OR (fulfillment_type = 'PICKUP' AND delivery_address_snapshot IS NULL)
                    ),
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS arrangements (
                    arrangement_id INTEGER PRIMARY KEY,
                    order_id INTEGER NOT NULL,
                    parent_arrangement_id INTEGER,
                    name_snapshot TEXT NOT NULL CHECK (length(trim(name_snapshot)) > 0),
                    occasion_snapshot TEXT NOT NULL CHECK (length(trim(occasion_snapshot)) > 0),
                    wrapping_style_snapshot TEXT,
                    message_text_snapshot TEXT,
                    base_price_snapshot INTEGER NOT NULL CHECK (base_price_snapshot >= 0),
                    extras_price_snapshot INTEGER NOT NULL DEFAULT 0 CHECK (extras_price_snapshot >= 0),
                    arrangement_total_snapshot INTEGER NOT NULL CHECK (
                        arrangement_total_snapshot >= 0
                        AND arrangement_total_snapshot = base_price_snapshot + extras_price_snapshot
                    ),
                    CHECK (parent_arrangement_id IS NULL OR parent_arrangement_id <> arrangement_id),
                    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                    FOREIGN KEY (parent_arrangement_id) REFERENCES arrangements(arrangement_id) ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS arrangement_flowers (
                    arrangement_flower_id INTEGER PRIMARY KEY,
                    arrangement_id INTEGER NOT NULL,
                    flower_id INTEGER NOT NULL,
                    flower_name_snapshot TEXT NOT NULL CHECK (length(trim(flower_name_snapshot)) > 0),
                    unit_price_snapshot INTEGER NOT NULL CHECK (unit_price_snapshot >= 0),
                    quantity INTEGER NOT NULL CHECK (quantity > 0),
                    line_total_snapshot INTEGER NOT NULL CHECK (line_total_snapshot = unit_price_snapshot * quantity),
                    UNIQUE (arrangement_id, flower_id),
                    FOREIGN KEY (arrangement_id) REFERENCES arrangements(arrangement_id) ON DELETE CASCADE,
                    FOREIGN KEY (flower_id) REFERENCES flowers(flower_id) ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS arrangement_extras (
                    arrangement_extra_id INTEGER PRIMARY KEY,
                    arrangement_id INTEGER NOT NULL,
                    extra_id INTEGER NOT NULL,
                    extra_name_snapshot TEXT NOT NULL CHECK (length(trim(extra_name_snapshot)) > 0),
                    unit_price_snapshot INTEGER NOT NULL CHECK (unit_price_snapshot >= 0),
                    quantity INTEGER NOT NULL CHECK (quantity > 0),
                    line_total_snapshot INTEGER NOT NULL CHECK (line_total_snapshot = unit_price_snapshot * quantity),
                    UNIQUE (arrangement_id, extra_id),
                    FOREIGN KEY (arrangement_id) REFERENCES arrangements(arrangement_id) ON DELETE CASCADE,
                    FOREIGN KEY (extra_id) REFERENCES extras(extra_id) ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS notifications (
                    notification_id INTEGER PRIMARY KEY,
                    order_id INTEGER NOT NULL,
                    status_snapshot TEXT NOT NULL CHECK (status_snapshot IN ('ORDERED', 'PREPARING', 'ARRANGING', 'READY', 'DELIVERED')),
                    message TEXT NOT NULL CHECK (length(trim(message)) > 0),
                    is_read INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
                )
                """
        };

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private void seedData(Connection connection) throws SQLException {
        seedFlower(connection, "Rose", "Red", 3000, 80);
        seedFlower(connection, "Tulip", "Pink", 2200, 70);
        seedFlower(connection, "Lily", "White", 3500, 45);
        seedFlower(connection, "Sunflower", "Yellow", 1800, 60);
        seedFlower(connection, "Orchid", "Purple", 4500, 25);

        seedExtra(connection, "Premium Wrapping", "Layered paper wrapping", 5000);
        seedExtra(connection, "Greeting Card", "Handwritten message card", 1200);
        seedExtra(connection, "Satin Ribbon", "Decorative satin ribbon", 1500);
        seedExtra(connection, "Chocolate Box", "Small assorted chocolate box", 6500);
        seedExtra(connection, "Balloon Set", "Three celebration balloons", 4000);

        seedCustomer(connection, "Nadia Rahman", "01710000001", "nadia@example.com", "Dhanmondi, Dhaka");
        seedCustomer(connection, "Arian Chowdhury", "01710000002", "arian@example.com", "Uttara, Dhaka");
        seedCustomer(connection, "Samira Khan", "01710000003", "samira@example.com", "Banani, Dhaka");

        seedTemplate(connection, "Classic Romance", "Anniversary", "A red rose bouquet", "Premium paper", "Happy Anniversary");
        seedTemplate(connection, "Bright Celebration", "Birthday", "A bright mixed bouquet", "Yellow paper", "Happy Birthday");
        seedTemplate(connection, "Elegant Lily", "Thank You", "A simple white lily bouquet", "White paper", "Thank you");

        seedTemplateFlower(connection, "Classic Romance", "Rose", 12);
        seedTemplateFlower(connection, "Classic Romance", "Lily", 3);
        seedTemplateFlower(connection, "Bright Celebration", "Sunflower", 5);
        seedTemplateFlower(connection, "Bright Celebration", "Tulip", 8);
        seedTemplateFlower(connection, "Elegant Lily", "Lily", 8);
        seedTemplateFlower(connection, "Elegant Lily", "Orchid", 2);
    }

    private void seedFlower(Connection connection, String name, String color, int price, int stockQuantity) throws SQLException {
        String sql = "INSERT OR IGNORE INTO flowers (name, color, current_unit_price, stock_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, color);
            statement.setInt(3, price);
            statement.setInt(4, stockQuantity);
            statement.executeUpdate();
        }
    }

    private void seedExtra(Connection connection, String name, String description, int price) throws SQLException {
        String sql = "INSERT OR IGNORE INTO extras (name, description, current_unit_price) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, price);
            statement.executeUpdate();
        }
    }

    private void seedCustomer(Connection connection, String name, String phone, String email, String address) throws SQLException {
        String sql = "INSERT OR IGNORE INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setString(4, address);
            statement.executeUpdate();
        }
    }

    private void seedTemplate(Connection connection, String name, String occasion, String description,
                              String wrappingStyle, String messageText) throws SQLException {
        String sql = "INSERT OR IGNORE INTO bouquet_templates "
                + "(name, occasion, description, wrapping_style, message_text) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, occasion);
            statement.setString(3, description);
            statement.setString(4, wrappingStyle);
            statement.setString(5, messageText);
            statement.executeUpdate();
        }
    }

    private void seedTemplateFlower(Connection connection, String templateName, String flowerName, int quantity)
            throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO bouquet_template_flowers (template_id, flower_id, quantity)
                SELECT bouquet_templates.template_id, flowers.flower_id, ?
                FROM bouquet_templates, flowers
                WHERE bouquet_templates.name = ? AND flowers.name = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setString(2, templateName);
            statement.setString(3, flowerName);
            statement.executeUpdate();
        }
    }
}
