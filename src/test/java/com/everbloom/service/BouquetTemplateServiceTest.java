package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.repository.BouquetTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
