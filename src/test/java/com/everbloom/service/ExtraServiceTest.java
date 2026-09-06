package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Extra;
import com.everbloom.repository.ExtraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraServiceTest {

    @TempDir
    Path temporaryDirectory;

    private ExtraService extraService;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("extra-service-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        extraService = new ExtraService(new ExtraRepository(databaseConnection));
    }

    @Test
    void normalizesValuesAndSupportsServiceOperations() throws SQLException {
        Extra extra = new Extra("  Flower Food  ", "   ", 900, true);

        Extra created = extraService.create(extra);

        assertEquals("Flower Food", created.getName());
        assertNull(created.getDescription());
        assertEquals(1, extraService.search(" flower ").size());

        created.setDescription("Keeps flowers fresh");
        created.setActive(false);
        assertTrue(extraService.update(created));
        assertEquals("Keeps flowers fresh", extraService.findById(created.getId()).orElseThrow().getDescription());

        assertTrue(extraService.delete(created.getId()));
        assertTrue(extraService.findById(created.getId()).isEmpty());
        assertEquals(5, extraService.search(null).size());
    }

    @Test
    void rejectsInvalidExtraValuesAndIds() {
        assertThrows(IllegalArgumentException.class,
                () -> extraService.create(new Extra(null, "Description", 1000, true)));
        assertThrows(IllegalArgumentException.class,
                () -> extraService.create(new Extra("Card", "Description", -1, true)));
        assertThrows(IllegalArgumentException.class, () -> extraService.findById(0));
        assertThrows(IllegalArgumentException.class, () -> extraService.delete(-1));
    }
}
