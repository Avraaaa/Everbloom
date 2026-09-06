package com.everbloom.model;

public class Flower {

    private long id;
    private String name;
    private String color;
    private long unitPrice;
    private int stockQuantity;
    private boolean active;

    public Flower(String name, String color, long unitPrice, int stockQuantity, boolean active) {
        this(0, name, color, unitPrice, stockQuantity, active);
    }

    public Flower(long id, String name, String color, long unitPrice, int stockQuantity, boolean active) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
