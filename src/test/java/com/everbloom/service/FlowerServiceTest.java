package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Flower;
import com.everbloom.repository.FlowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlowerServiceTest {

    @TempDir
    Path temporaryDirectory;

    private FlowerService flowerService;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("flower-service-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        flowerService = new FlowerService(new FlowerRepository(databaseConnection));
    }

    @Test
    void normalizesValuesAndSupportsServiceOperations() throws SQLException {
        Flower flower = new Flower("  Dahlia  ", "   ", 4100, 20, true);

        Flower created = flowerService.create(flower);

        assertEquals("Dahlia", created.getName());
        assertNull(created.getColor());
        assertEquals(1, flowerService.search("  dahl  ").size());

        created.setStockQuantity(14);
        assertTrue(flowerService.update(created));
        assertEquals(14, flowerService.findById(created.getId()).orElseThrow().getStockQuantity());

        assertTrue(flowerService.delete(created.getId()));
        assertTrue(flowerService.findById(created.getId()).isEmpty());
        assertEquals(5, flowerService.search("   ").size());
    }

    @Test
    void rejectsInvalidFlowerValuesAndIds() {
        assertThrows(IllegalArgumentException.class,
                () -> flowerService.create(new Flower(" ", "White", 1000, 1, true)));
        assertThrows(IllegalArgumentException.class,
                () -> flowerService.create(new Flower("Daisy", "White", -1, 1, true)));
        assertThrows(IllegalArgumentException.class,
                () -> flowerService.create(new Flower("Daisy", "White", 1000, -1, true)));
        assertThrows(IllegalArgumentException.class, () -> flowerService.findById(0));
        assertThrows(IllegalArgumentException.class, () -> flowerService.delete(-1));
    }
}
