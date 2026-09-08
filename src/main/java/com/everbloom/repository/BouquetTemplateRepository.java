package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.model.Flower;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BouquetTemplateRepository {
    private final DatabaseConnection databaseConnection;
    public BouquetTemplateRepository(DatabaseConnection databaseConnection) { this.databaseConnection = databaseConnection; }

    public BouquetTemplate create(BouquetTemplate template) throws SQLException {
        String sql = "INSERT INTO bouquet_templates (name, occasion, wrapping_style, message_text) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setTemplateValues(statement, template);
                statement.executeUpdate();
                template.setId(readGeneratedId(statement));
                insertFlowers(connection, template);
                connection.commit();
                return template;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public boolean update(BouquetTemplate template) throws SQLException {
        String sql = "UPDATE bouquet_templates SET name = ?, occasion = ?, wrapping_style = ?, message_text = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE template_id = ?";
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setTemplateValues(statement, template);
                statement.setLong(5, template.getId());
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                deleteFlowers(connection, template.getId());
                insertFlowers(connection, template);
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

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

    private void setTemplateValues(PreparedStatement statement, BouquetTemplate template) throws SQLException {
        statement.setString(1, template.getName());
        statement.setString(2, template.getOccasion());
        statement.setString(3, template.getWrappingStyle());
        statement.setString(4, template.getMessage());
    }

    private void insertFlowers(Connection connection, BouquetTemplate template) throws SQLException {
        String sql = "INSERT INTO bouquet_template_flowers (template_id, flower_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (BouquetFlower flower : template.getFlowers()) {
                statement.setLong(1, template.getId());
                statement.setLong(2, flower.getFlower().getId());
                statement.setInt(3, flower.getQuantity());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteFlowers(Connection connection, long templateId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM bouquet_template_flowers WHERE template_id = ?")) {
            statement.setLong(1, templateId);
            statement.executeUpdate();
        }
    }

    private long readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet results = statement.getGeneratedKeys()) {
            if (results.next()) return results.getLong(1);
        }
        throw new SQLException("Unable to create bouquet template.");
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
