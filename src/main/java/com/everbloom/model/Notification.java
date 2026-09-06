package com.everbloom.model;

public class Notification {

    private final long id;
    private final long orderId;
    private final String orderNumber;
    private final String status;
    private final String message;
    private final String createdAt;

    public Notification(long id, long orderId, String orderNumber, String status, String message, String createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}
