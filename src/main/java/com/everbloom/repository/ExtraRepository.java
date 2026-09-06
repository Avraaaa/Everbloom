package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExtraRepository {

    private final DatabaseConnection databaseConnection;

    public ExtraRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Extra create(Extra extra) throws SQLException {
        String sql = "INSERT INTO extras (name, description, current_unit_price, is_active) VALUES (?, ?, ?, ?)";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, extra);
            statement.executeUpdate();
            extra.setId(readLastInsertedId(connection));
            return extra;
        }
    }

    public Optional<Extra> findById(long id) throws SQLException {
        String sql = "SELECT extra_id, name, description, current_unit_price, is_active "
                + "FROM extras WHERE extra_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(readExtra(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Extra> findAll() throws SQLException {
        String sql = "SELECT extra_id, name, description, current_unit_price, is_active "
                + "FROM extras ORDER BY name COLLATE NOCASE";
        return findMany(sql, null);
    }

    public List<Extra> search(String searchText) throws SQLException {
        String sql = "SELECT extra_id, name, description, current_unit_price, is_active "
                + "FROM extras WHERE name LIKE ? COLLATE NOCASE OR description LIKE ? COLLATE NOCASE "
                + "ORDER BY name COLLATE NOCASE";
        return findMany(sql, "%" + searchText + "%");
    }

    public boolean update(Extra extra) throws SQLException {
        String sql = "UPDATE extras SET name = ?, description = ?, current_unit_price = ?, is_active = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE extra_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, extra);
            statement.setLong(5, extra.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM extras WHERE extra_id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<Extra> findMany(String sql, String searchPattern) throws SQLException {
        List<Extra> extras = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (searchPattern != null) {
                statement.setString(1, searchPattern);
                statement.setString(2, searchPattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    extras.add(readExtra(resultSet));
                }
            }
        }
        return extras;
    }

    private void setValues(PreparedStatement statement, Extra extra) throws SQLException {
        statement.setString(1, extra.getName());
        statement.setString(2, extra.getDescription());
        statement.setLong(3, extra.getUnitPrice());
        statement.setInt(4, extra.isActive() ? 1 : 0);
    }

    private Extra readExtra(ResultSet resultSet) throws SQLException {
        return new Extra(
                resultSet.getLong("extra_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getLong("current_unit_price"),
                resultSet.getInt("is_active") == 1
        );
    }

    private long readLastInsertedId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT last_insert_rowid()");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Unable to read the created extra ID.");
            }
            return resultSet.getLong(1);
        }
    }
}
