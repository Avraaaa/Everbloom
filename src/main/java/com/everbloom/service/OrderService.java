package com.everbloom.service;

import com.everbloom.model.Order;
import com.everbloom.repository.OrderRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.everbloom.composite.ArrangementGroup;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) throws SQLException {
        validateOrder(order);
        return orderRepository.create(order);
    }

    public Order placeEventPackage(Order order, ArrangementGroup eventPackage) throws SQLException {
        validateEventPackage(order, eventPackage);
        return orderRepository.createEventPackage(order, eventPackage);
    }

    public Optional<ArrangementGroup> findEventPackage(long orderId) throws SQLException {
        return orderRepository.findEventPackage(orderId);
    }

    private void validateEventPackage(Order order, ArrangementGroup eventPackage) {
        if (order == null || order.getCustomer() == null || order.getCustomer().getId() <= 0) {
            throw new IllegalArgumentException("Select a saved customer for the event package.");
        }
        if (eventPackage == null || eventPackage.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Add at least one arrangement to the event package.");
        }
        if (order.getSubtotal() != eventPackage.getTotalPrice()
                || order.getTotal() != order.getSubtotal() - order.getDiscount()) {
            throw new IllegalArgumentException("Event package pricing is invalid.");
        }
        if (!"PICKUP".equals(order.getFulfillmentType()) && !"DELIVERY".equals(order.getFulfillmentType())) {
            throw new IllegalArgumentException("Select pickup or delivery.");
        }
        if ("DELIVERY".equals(order.getFulfillmentType())
                && (order.getDeliveryAddress() == null || order.getDeliveryAddress().isBlank())) {
            throw new IllegalArgumentException("Enter a delivery address.");
        }
    }

    public List<Order> findOrders(String searchText, String status) throws SQLException {
        List<Order> matches = new ArrayList<>();
        String query = searchText == null ? "" : searchText.trim().toLowerCase();
        for (Order order : orderRepository.findAll()) {
            boolean matchesSearch = query.isEmpty() || order.getOrderNumber().toLowerCase().contains(query) || order.getCustomer().getFullName().toLowerCase().contains(query);
            boolean matchesStatus = status == null || "All statuses".equals(status) || status.equals(order.getStatus());
            if (matchesSearch && matchesStatus) matches.add(order);
        }
        return matches;
    }

    public void advanceOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Select an order first.");
        order.advanceStatus();
        orderRepository.updateStatus(order);
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
