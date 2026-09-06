package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    private final DatabaseConnection databaseConnection;

    public CustomerRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Customer create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?)";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, customer);
            statement.executeUpdate();
            customer.setId(readLastInsertedId(connection));
            return customer;
        }
    }

    public Optional<Customer> findById(long id) throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address FROM customers WHERE customer_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(readCustomer(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Customer> findByPhone(String phone) throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address FROM customers WHERE phone = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(readCustomer(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address FROM customers "
                + "ORDER BY full_name COLLATE NOCASE";
        return findMany(sql, null);
    }

    public List<Customer> search(String searchText) throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address FROM customers "
                + "WHERE full_name LIKE ? COLLATE NOCASE OR phone LIKE ? COLLATE NOCASE "
                + "ORDER BY full_name COLLATE NOCASE";
        return findMany(sql, "%" + searchText + "%");
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, address = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, customer);
            statement.setLong(5, customer.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<Customer> findMany(String sql, String searchPattern) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (searchPattern != null) {
                statement.setString(1, searchPattern);
                statement.setString(2, searchPattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(readCustomer(resultSet));
                }
            }
        }
        return customers;
    }

    private void setValues(PreparedStatement statement, Customer customer) throws SQLException {
        statement.setString(1, customer.getFullName());
        statement.setString(2, customer.getPhone());
        statement.setString(3, customer.getEmail());
        statement.setString(4, customer.getAddress());
    }

    private Customer readCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getLong("customer_id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("address")
        );
    }

    private long readLastInsertedId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT last_insert_rowid()");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Unable to read the created customer ID.");
            }
            return resultSet.getLong(1);
        }
    }
}
