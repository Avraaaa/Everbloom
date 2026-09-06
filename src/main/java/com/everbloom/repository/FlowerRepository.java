package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Flower;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlowerRepository {

    private final DatabaseConnection databaseConnection;

    public FlowerRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Flower create(Flower flower) throws SQLException {
        String sql = "INSERT INTO flowers (name, color, current_unit_price, stock_quantity, is_active) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, flower);
            statement.executeUpdate();
            flower.setId(readLastInsertedId(connection));
            return flower;
        }
    }

    public Optional<Flower> findById(long id) throws SQLException {
        String sql = "SELECT flower_id, name, color, current_unit_price, stock_quantity, is_active "
                + "FROM flowers WHERE flower_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(readFlower(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Flower> findAll() throws SQLException {
        String sql = "SELECT flower_id, name, color, current_unit_price, stock_quantity, is_active "
                + "FROM flowers ORDER BY name COLLATE NOCASE";
        return findMany(sql, null);
    }

    public List<Flower> search(String searchText) throws SQLException {
        String sql = "SELECT flower_id, name, color, current_unit_price, stock_quantity, is_active "
                + "FROM flowers WHERE name LIKE ? COLLATE NOCASE OR color LIKE ? COLLATE NOCASE "
                + "ORDER BY name COLLATE NOCASE";
        return findMany(sql, "%" + searchText + "%");
    }

    public boolean update(Flower flower) throws SQLException {
        String sql = "UPDATE flowers SET name = ?, color = ?, current_unit_price = ?, stock_quantity = ?, "
                + "is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE flower_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, flower);
            statement.setLong(6, flower.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM flowers WHERE flower_id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<Flower> findMany(String sql, String searchPattern) throws SQLException {
        List<Flower> flowers = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (searchPattern != null) {
                statement.setString(1, searchPattern);
                statement.setString(2, searchPattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    flowers.add(readFlower(resultSet));
                }
            }
        }
        return flowers;
    }

    private void setValues(PreparedStatement statement, Flower flower) throws SQLException {
        statement.setString(1, flower.getName());
        statement.setString(2, flower.getColor());
        statement.setLong(3, flower.getUnitPrice());
        statement.setInt(4, flower.getStockQuantity());
        statement.setInt(5, flower.isActive() ? 1 : 0);
    }

    private Flower readFlower(ResultSet resultSet) throws SQLException {
        return new Flower(
                resultSet.getLong("flower_id"),
                resultSet.getString("name"),
                resultSet.getString("color"),
                resultSet.getLong("current_unit_price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getInt("is_active") == 1
        );
    }

    private long readLastInsertedId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT last_insert_rowid()");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Unable to read the created flower ID.");
            }
            return resultSet.getLong(1);
        }
    }
}
