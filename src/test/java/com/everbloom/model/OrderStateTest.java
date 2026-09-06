package com.everbloom.model;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class OrderStateTest {
    @Test void movesThroughAllowedLifecycle() {
        Order order = new Order("EB-1", new Customer(1, "Nadia", "01", null, null), null, List.of(), "PICKUP", null, "Standard", 0, 0, 0);
        order.advanceStatus(); assertEquals("PREPARING", order.getStatus());
        order.advanceStatus(); assertEquals("ARRANGING", order.getStatus());
        order.advanceStatus(); assertEquals("READY", order.getStatus());
        order.advanceStatus(); assertEquals("DELIVERED", order.getStatus());
    }
    @Test void rejectsTransitionAfterDelivery() {
        Order order = new Order(1, "EB-1", new Customer(1, "Nadia", "01", null, null), null, List.of(), "PICKUP", null, "Standard", 0, 0, 0, "DELIVERED", null);
        assertThrows(IllegalStateException.class, order::advanceStatus);
    }
}
