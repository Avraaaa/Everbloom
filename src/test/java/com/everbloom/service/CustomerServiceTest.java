package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Customer;
import com.everbloom.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerServiceTest {

    @TempDir
    Path temporaryDirectory;

    private CustomerService customerService;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("customer-service-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        customerService = new CustomerService(new CustomerRepository(databaseConnection));
    }

    @Test
    void normalizesCustomerValuesAndPreventsDuplicatePhones() throws SQLException {
        Customer customer = new Customer("  Rina Das  ", "01999 000-001", "  rina@example.com  ", "   ");

        Customer created = customerService.create(customer);

        assertEquals("Rina Das", created.getFullName());
        assertEquals("01999000001", created.getPhone());
        assertEquals("rina@example.com", created.getEmail());
        assertNull(created.getAddress());
        assertEquals(1, customerService.search("01999000001").size());

        assertThrows(IllegalArgumentException.class,
                () -> customerService.create(new Customer("Another Rina", "01999000001", null, null)));

        created.setAddress("Gulshan, Dhaka");
        assertTrue(customerService.update(created));
        assertEquals("Gulshan, Dhaka", customerService.findById(created.getId()).orElseThrow().getAddress());
    }

    @Test
    void rejectsInvalidCustomerValuesAndIds() {
        assertThrows(IllegalArgumentException.class,
                () -> customerService.create(new Customer(" ", "01710000009", null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> customerService.create(new Customer("Rina", " ", null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> customerService.create(new Customer("Rina", "phone", null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> customerService.create(new Customer("Rina", "01710000009", "invalid-email", null)));
        assertThrows(IllegalArgumentException.class, () -> customerService.findById(0));
        assertThrows(IllegalArgumentException.class, () -> customerService.delete(-1));
    }
}
