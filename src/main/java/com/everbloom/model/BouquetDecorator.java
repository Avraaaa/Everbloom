package com.everbloom.model;

public abstract class BouquetDecorator implements BouquetItem {

    protected final BouquetItem bouquetItem;

    protected BouquetDecorator(BouquetItem bouquetItem) {
        if (bouquetItem == null) {
            throw new IllegalArgumentException("Bouquet item is required.");
        }
        this.bouquetItem = bouquetItem;
    }
}
