package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Flower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlowerRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    private FlowerRepository flowerRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("flowers-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        flowerRepository = new FlowerRepository(databaseConnection);
    }

    @Test
    void supportsCreateReadUpdateSearchAndDelete() throws SQLException {
        Flower flower = new Flower("Peony", "Blush", 5200, 18, true);

        Flower created = flowerRepository.create(flower);

        assertTrue(created.getId() > 0);
        Flower stored = flowerRepository.findById(created.getId()).orElseThrow();
        assertEquals("Peony", stored.getName());
        assertEquals(5200, stored.getUnitPrice());
        assertEquals(18, stored.getStockQuantity());
        assertTrue(stored.isActive());

        stored.setName("Garden Peony");
        stored.setColor("Soft Pink");
        stored.setUnitPrice(5600);
        stored.setStockQuantity(12);
        stored.setActive(false);

        assertTrue(flowerRepository.update(stored));

        List<Flower> matches = flowerRepository.search("soft pink");
        assertEquals(1, matches.size());
        assertEquals("Garden Peony", matches.getFirst().getName());
        assertEquals(5600, matches.getFirst().getUnitPrice());
        assertEquals(12, matches.getFirst().getStockQuantity());
        assertFalse(matches.getFirst().isActive());

        assertTrue(flowerRepository.delete(created.getId()));
        assertTrue(flowerRepository.findById(created.getId()).isEmpty());
        assertFalse(flowerRepository.delete(created.getId()));
    }
}
