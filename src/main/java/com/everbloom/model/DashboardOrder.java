package com.everbloom.model;

public class DashboardOrder {

    private final String orderNumber;
    private final String customerName;
    private final String occasion;
    private final String status;
    private final long total;

    public DashboardOrder(String orderNumber, String customerName, String occasion, String status, long total) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.occasion = occasion;
        this.status = status;
        this.total = total;
    }

    public String getOrderNumber() { return orderNumber; }
    public String getCustomerName() { return customerName; }
    public String getOccasion() { return occasion; }
    public String getStatus() { return status; }
    public long getTotal() { return total; }
}
