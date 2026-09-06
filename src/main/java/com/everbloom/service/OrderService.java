package com.everbloom.service;

import com.everbloom.model.Order;
import com.everbloom.repository.OrderRepository;

import java.sql.SQLException;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) throws SQLException {
        validateOrder(order);
        return orderRepository.create(order);
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        if (order.getCustomer() == null || order.getCustomer().getId() <= 0) {
            throw new IllegalArgumentException("Select a saved customer for the order.");
        }
        if (order.getBouquet() == null || order.getBouquet().getFlowers().isEmpty()) {
            throw new IllegalArgumentException("Build a bouquet before placing the order.");
        }
        if (!"PICKUP".equals(order.getFulfillmentType()) && !"DELIVERY".equals(order.getFulfillmentType())) {
            throw new IllegalArgumentException("Select pickup or delivery.");
        }
        if ("DELIVERY".equals(order.getFulfillmentType())
                && (order.getDeliveryAddress() == null || order.getDeliveryAddress().isBlank())) {
            throw new IllegalArgumentException("Enter a delivery address.");
        }
        if (order.getSubtotal() < 0 || order.getDiscount() < 0 || order.getTotal() < 0
                || order.getTotal() != order.getSubtotal() - order.getDiscount()) {
            throw new IllegalArgumentException("Order pricing is invalid.");
        }
    }
}
