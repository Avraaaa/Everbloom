package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Bouquet;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.Customer;
import com.everbloom.model.Extra;
import com.everbloom.model.Flower;
import com.everbloom.model.Order;
import com.everbloom.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void savesOrderAndHistoricalBouquetPrices() throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("orders-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        OrderRepository orderRepository = new OrderRepository(databaseConnection);
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 10, true);
        Bouquet bouquet = new BouquetBuilder().forCustomer(customer).forOccasion("Birthday")
                .withWrapping("Kraft paper").withMessage("Happy Birthday").addFlower(rose, 2).build();
        Extra card = new Extra(2, "Greeting Card", "Handwritten message", 1200, true);
        Order order = new Order("EB-1001", customer, bouquet, List.of(card), "PICKUP", null,
                "Standard Pricing", 7200, 0, 7200);

        Order savedOrder = orderRepository.create(order);

        assertTrue(savedOrder.getId() > 0);
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT total_snapshot FROM orders WHERE order_id = " + savedOrder.getId())) {
            assertTrue(resultSet.next());
            assertEquals(7200, resultSet.getLong("total_snapshot"));
        }
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT line_total_snapshot FROM arrangement_flowers")) {
            assertTrue(resultSet.next());
            assertEquals(6000, resultSet.getLong("line_total_snapshot"));
        }

        Order loadedOrder = orderRepository.findAll().getFirst();
        assertEquals("Birthday", loadedOrder.getBouquet().getOccasion());
        assertEquals("Kraft paper", loadedOrder.getBouquet().getWrappingStyle());
        assertEquals("Happy Birthday", loadedOrder.getBouquet().getMessage());
        assertEquals(1, loadedOrder.getBouquet().getFlowers().size());
        assertEquals("Rose", loadedOrder.getBouquet().getFlowers().getFirst().getFlower().getName());
        assertEquals(3000, loadedOrder.getBouquet().getFlowers().getFirst().getFlower().getUnitPrice());
        assertEquals("Greeting Card", loadedOrder.getExtras().getFirst().getName());
        assertEquals(1200, loadedOrder.getExtras().getFirst().getUnitPrice());
        assertTrue(loadedOrder.getPlacedAt() != null);

        OrderService orderService = new OrderService(orderRepository);
        assertEquals(1, orderService.findOrders("", "All statuses", "PICKUP").size());
        assertTrue(orderService.findOrders("", "All statuses", "DELIVERY").isEmpty());
    }
}
