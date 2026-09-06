package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Bouquet;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.Customer;
import com.everbloom.model.Flower;
import com.everbloom.model.Order;
import com.everbloom.observer.NotificationObserver;
import com.everbloom.repository.NotificationRepository;
import com.everbloom.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderObserverTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void notifiesSubscribersAfterSuccessfulTransitionButNotFailedTransition() throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("observer-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        OrderService orderService = new OrderService(new OrderRepository(databaseConnection));
        NotificationService notificationService = new NotificationService(new NotificationRepository(databaseConnection));
        List<String> observedStatuses = new ArrayList<>();
        orderService.addObserver(order -> observedStatuses.add(order.getStatus()));
        orderService.addObserver(new NotificationObserver(notificationService));
        Order order = orderService.placeOrder(createOrder("EB-OBSERVER"));

        orderService.advanceOrder(order);

        assertEquals(List.of("PREPARING"), observedStatuses);
        assertEquals(1, notificationService.findRecent(5).size());
        assertEquals("PREPARING", notificationService.findRecent(5).getFirst().getStatus());

        orderService.advanceOrder(order);
        orderService.advanceOrder(order);
        orderService.advanceOrder(order);
        int observerCallsBeforeFailure = observedStatuses.size();
        int notificationsBeforeFailure = notificationService.findRecent(10).size();

        assertThrows(IllegalStateException.class, () -> orderService.advanceOrder(order));

        assertEquals(observerCallsBeforeFailure, observedStatuses.size());
        assertEquals(notificationsBeforeFailure, notificationService.findRecent(10).size());
    }

    private Order createOrder(String orderNumber) {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 80, true);
        Bouquet bouquet = new BouquetBuilder().forCustomer(customer).forOccasion("Birthday").addFlower(rose, 2).build();
        return new Order(orderNumber, customer, bouquet, List.of(), "PICKUP", null, "Standard Pricing", 6000, 0, 6000);
    }
}
