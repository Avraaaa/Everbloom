package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Extra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    private ExtraRepository extraRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("extras-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        extraRepository = new ExtraRepository(databaseConnection);
    }

    @Test
    void supportsCreateReadUpdateSearchAndDelete() throws SQLException {
        Extra extra = new Extra("Ceramic Vase", "Small ivory vase", 7500, true);

        Extra created = extraRepository.create(extra);

        assertTrue(created.getId() > 0);
        Extra stored = extraRepository.findById(created.getId()).orElseThrow();
        assertEquals("Ceramic Vase", stored.getName());
        assertEquals("Small ivory vase", stored.getDescription());
        assertEquals(7500, stored.getUnitPrice());
        assertTrue(stored.isActive());

        stored.setName("Keepsake Vase");
        stored.setDescription("Reusable ivory ceramic vase");
        stored.setUnitPrice(8000);
        stored.setActive(false);

        assertTrue(extraRepository.update(stored));

        List<Extra> matches = extraRepository.search("reusable");
        assertEquals(1, matches.size());
        assertEquals("Keepsake Vase", matches.getFirst().getName());
        assertEquals(8000, matches.getFirst().getUnitPrice());
        assertFalse(matches.getFirst().isActive());

        assertTrue(extraRepository.delete(created.getId()));
        assertTrue(extraRepository.findById(created.getId()).isEmpty());
        assertFalse(extraRepository.delete(created.getId()));
    }
}
