package com.everbloom.model;

public class BaseBouquet implements BouquetItem {

    private final Bouquet bouquet;

    public BaseBouquet(Bouquet bouquet) {
        if (bouquet == null) {
            throw new IllegalArgumentException("Bouquet is required.");
        }
        this.bouquet = bouquet;
    }

    @Override
    public String getDescription() {
        return bouquet.getOccasion() + " bouquet";
    }

    @Override
    public long getSubtotal() {
        return bouquet.getFlowerSubtotal();
    }
}
