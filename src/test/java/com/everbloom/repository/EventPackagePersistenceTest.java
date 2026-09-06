package com.everbloom.repository;

import com.everbloom.composite.ArrangementGroup;
import com.everbloom.composite.BouquetArrangement;
import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Customer;
import com.everbloom.model.Order;
import com.everbloom.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPackagePersistenceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void savesAndLoadsNestedEventPackageHierarchy() throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(
                temporaryDirectory.resolve("event-package-test.db")
        );
        new DatabaseInitializer(databaseConnection).initialize();
        OrderService orderService = new OrderService(new OrderRepository(databaseConnection));

        ArrangementGroup wedding = new ArrangementGroup("Wedding Package");
        ArrangementGroup ceremony = new ArrangementGroup("Ceremony");
        ceremony.add(new BouquetArrangement("Bridal Bouquet", 12000));
        ArrangementGroup reception = new ArrangementGroup("Reception");
        ArrangementGroup tables = new ArrangementGroup("Tables");
        tables.add(new BouquetArrangement("Head Table Arrangement", 9000));
        reception.add(tables);
        wedding.add(ceremony);
        wedding.add(reception);

        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Order order = new Order("EB-EVENT-1", customer, null, List.of(), "PICKUP", null,
                "Standard Pricing", 21000, 0, 21000);

        Order savedOrder = orderService.placeEventPackage(order, wedding);
        ArrangementGroup loaded = orderService.findEventPackage(savedOrder.getId()).orElseThrow();

        assertEquals("Wedding Package", loaded.getName());
        assertEquals(2, loaded.getChildren().size());
        assertEquals(21000, loaded.getTotalPrice());
        assertTrue(loaded.getSummary().contains("Head Table Arrangement: BDT 90.00"));
        ArrangementGroup loadedReception = (ArrangementGroup) loaded.getChildren().get(1);
        ArrangementGroup loadedTables = (ArrangementGroup) loadedReception.getChildren().getFirst();
        assertEquals("Tables", loadedTables.getName());
    }
}
