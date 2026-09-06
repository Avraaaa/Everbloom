package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.model.Flower;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BouquetTemplateRepository {
    private final DatabaseConnection databaseConnection;
    public BouquetTemplateRepository(DatabaseConnection databaseConnection) { this.databaseConnection = databaseConnection; }

    public List<BouquetTemplate> findAll() throws SQLException {
        List<BouquetTemplate> templates = new ArrayList<>();
        String sql = "SELECT template_id, name, occasion, wrapping_style, message_text FROM bouquet_templates WHERE is_active = 1 ORDER BY name COLLATE NOCASE";
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                templates.add(new BouquetTemplate(results.getLong("template_id"), results.getString("name"), results.getString("occasion"), results.getString("wrapping_style"), results.getString("message_text"), findFlowers(connection, results.getLong("template_id"))));
            }
        }
        return templates;
    }

    public boolean delete(long id) throws SQLException {
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM bouquet_templates WHERE template_id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<BouquetFlower> findFlowers(Connection connection, long templateId) throws SQLException {
        List<BouquetFlower> flowers = new ArrayList<>();
        String sql = "SELECT f.flower_id, f.name, f.color, f.current_unit_price, f.stock_quantity, f.is_active, tf.quantity FROM bouquet_template_flowers tf JOIN flowers f ON f.flower_id = tf.flower_id WHERE tf.template_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, templateId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    flowers.add(new BouquetFlower(new Flower(results.getLong("flower_id"), results.getString("name"), results.getString("color"), results.getLong("current_unit_price"), results.getInt("stock_quantity"), results.getInt("is_active") == 1), results.getInt("quantity")));
                }
            }
        }
        return flowers;
    }
}
