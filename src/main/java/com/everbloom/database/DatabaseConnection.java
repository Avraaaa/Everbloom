package com.everbloom.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private final Path databasePath;

    public DatabaseConnection() {
        this(Path.of("data", "everbloom.db"));
    }

    public DatabaseConnection(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath();
    }

    public Connection getConnection() throws SQLException {
        createDatabaseDirectory();

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void createDatabaseDirectory() throws SQLException {
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (IOException exception) {
            throw new SQLException("Unable to create the database directory.", exception);
        }
    }
}
