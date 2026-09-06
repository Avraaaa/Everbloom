package com.everbloom.model;

public class PopularItem {

    private final String name;
    private final long quantity;
    private final long revenue;

    public PopularItem(String name, long quantity, long revenue) {
        this.name = name;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    public String getName() { return name; }
    public long getQuantity() { return quantity; }
    public long getRevenue() { return revenue; }
}
