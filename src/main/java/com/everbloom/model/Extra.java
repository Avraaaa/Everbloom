package com.everbloom.model;

public class Extra {

    private long id;
    private String name;
    private String description;
    private long unitPrice;
    private boolean active;

    public Extra(String name, String description, long unitPrice, boolean active) {
        this(0, name, description, unitPrice, active);
    }

    public Extra(long id, String name, String description, long unitPrice, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
