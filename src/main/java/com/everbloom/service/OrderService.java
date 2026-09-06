package com.everbloom.service;

import com.everbloom.model.Order;
import com.everbloom.observer.OrderObserver;
import com.everbloom.repository.OrderRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) throws SQLException {
        validateOrder(order);
        return orderRepository.create(order);
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
        notifyObservers(order);
    }

    public void addObserver(OrderObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Order observer is required.");
        }
        observers.add(observer);
    }

    public boolean removeObserver(OrderObserver observer) {
        return observers.remove(observer);
    }

    private void notifyObservers(Order order) throws SQLException {
        for (OrderObserver observer : observers) {
            observer.onOrderStatusChanged(order);
        }
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
