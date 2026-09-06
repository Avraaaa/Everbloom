package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.model.Flower;
import com.everbloom.repository.BouquetTemplateRepository;
import com.everbloom.repository.FlowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BouquetTemplateServiceTest {
    @TempDir Path temporaryDirectory;

    @Test
    void loadsTemplateCompositionAndCreatesIndependentCopies() throws Exception {
        DatabaseConnection connection = new DatabaseConnection(temporaryDirectory.resolve("templates.db"));
        new DatabaseInitializer(connection).initialize();
        BouquetTemplateService service = new BouquetTemplateService(new BouquetTemplateRepository(connection));
        List<BouquetTemplate> templates = service.findAll();
        assertFalse(templates.isEmpty());
        BouquetTemplate template = templates.getFirst();
        BouquetBuilder first = service.copyToBuilder(template);
        BouquetBuilder second = service.copyToBuilder(template);
        first.removeFlower(first.getSelectedFlowers().getFirst().getFlower());
        assertEquals(template.getFlowers().size() - 1, first.getSelectedFlowers().size());
        assertEquals(template.getFlowers().size(), second.getSelectedFlowers().size());
        assertEquals(template.getFlowers().size(), template.getFlowers().size());
    }

    @Test
    void createsAndUpdatesTemplateWithFlowerComposition() throws Exception {
        DatabaseConnection connection = new DatabaseConnection(temporaryDirectory.resolve("template-save.db"));
        new DatabaseInitializer(connection).initialize();
        BouquetTemplateService service = new BouquetTemplateService(new BouquetTemplateRepository(connection));
        List<Flower> flowers = new FlowerRepository(connection).findAll();
        Flower firstFlower = flowers.getFirst();
        Flower secondFlower = flowers.get(1);

        BouquetTemplate created = service.create(new BouquetTemplate(0, "Spring Gift", "Thank You",
                "Kraft paper", "With thanks", List.of(new BouquetFlower(firstFlower, 3))));

        assertTrue(created.getId() > 0);
        BouquetTemplate stored = service.findAll().stream()
                .filter(template -> template.getId() == created.getId()).findFirst().orElseThrow();
        assertEquals("Spring Gift", stored.getName());
        assertEquals(3, stored.getFlowers().getFirst().getQuantity());

        BouquetTemplate updated = new BouquetTemplate(created.getId(), "Spring Celebration", "Congratulations",
                "Premium paper", "Well done", List.of(new BouquetFlower(secondFlower, 5)));
        assertTrue(service.update(updated));

        BouquetTemplate reloaded = service.findAll().stream()
                .filter(template -> template.getId() == created.getId()).findFirst().orElseThrow();
        assertEquals("Spring Celebration", reloaded.getName());
        assertEquals("Congratulations", reloaded.getOccasion());
        assertEquals(secondFlower.getName(), reloaded.getFlowers().getFirst().getFlower().getName());
        assertEquals(5, reloaded.getFlowers().getFirst().getQuantity());
    }
}
