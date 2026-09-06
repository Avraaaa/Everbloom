package com.everbloom.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseInitializerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void initializesSchemaSeedsDataAndEnforcesForeignKeys() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("everbloom-test.db"));
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(databaseConnection);

        databaseInitializer.initialize();
        databaseInitializer.initialize();

        try (Connection connection = databaseConnection.getConnection()) {
            assertEquals(1, readForeignKeysEnabled(connection));
            assertEquals(5, countRows(connection, "flowers"));
            assertEquals(5, countRows(connection, "extras"));
            assertEquals(3, countRows(connection, "customers"));
            assertEquals(3, countRows(connection, "bouquet_templates"));
            assertEquals(6, countRows(connection, "bouquet_template_flowers"));

            assertThrows(SQLException.class, () -> insertInvalidTemplateFlower(connection));
        }
    }

    private int readForeignKeysEnabled(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int countRows(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void insertInvalidTemplateFlower(Connection connection) throws SQLException {
        String sql = "INSERT INTO bouquet_template_flowers (template_id, flower_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, 9999);
            statement.setInt(2, 9999);
            statement.setInt(3, 1);
            statement.executeUpdate();
        }
    }
}
