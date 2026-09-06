package com.everbloom.model;

public class BouquetFlower {

    private final Flower flower;
    private final int quantity;

    public BouquetFlower(Flower flower, int quantity) {
        this.flower = flower;
        this.quantity = quantity;
    }

    public Flower getFlower() {
        return flower;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getLineTotal() {
        return flower.getUnitPrice() * quantity;
    }
}
