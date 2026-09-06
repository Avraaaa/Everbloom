package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("customers-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        customerRepository = new CustomerRepository(databaseConnection);
    }

    @Test
    void supportsCreateReadUpdateSearchAndDelete() throws SQLException {
        Customer customer = new Customer("Farah Akter", "01810000001", "farah@example.com", "Mirpur, Dhaka");

        Customer created = customerRepository.create(customer);

        assertTrue(created.getId() > 0);
        assertEquals("Farah Akter", customerRepository.findById(created.getId()).orElseThrow().getFullName());
        assertEquals(created.getId(), customerRepository.findByPhone("01810000001").orElseThrow().getId());

        created.setFullName("Farah Sultana");
        created.setPhone("01810000002");
        created.setEmail("farah.sultana@example.com");
        created.setAddress("Mohammadpur, Dhaka");

        assertTrue(customerRepository.update(created));

        List<Customer> matches = customerRepository.search("01810000002");
        assertEquals(1, matches.size());
        assertEquals("Farah Sultana", matches.getFirst().getFullName());
        assertEquals("farah.sultana@example.com", matches.getFirst().getEmail());

        assertTrue(customerRepository.delete(created.getId()));
        assertTrue(customerRepository.findById(created.getId()).isEmpty());
        assertFalse(customerRepository.delete(created.getId()));
    }
}
