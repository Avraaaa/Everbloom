package com.everbloom.composite;

import com.everbloom.model.BouquetItem;

public class BouquetArrangement implements EventPackageComponent {
    private final String name;
    private final BouquetItem bouquetItem;
    private final long storedPrice;

    public BouquetArrangement(String name, BouquetItem bouquetItem) {
        if (name == null || name.isBlank() || bouquetItem == null) {
            throw new IllegalArgumentException("Arrangement name and bouquet are required.");
        }
        this.name = name;
        this.bouquetItem = bouquetItem;
        this.storedPrice = bouquetItem.getSubtotal();
    }

    public BouquetArrangement(String name, long storedPrice) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Arrangement name is required.");
        }
        if (storedPrice < 0) {
            throw new IllegalArgumentException("Arrangement price cannot be negative.");
        }
        this.name = name;
        this.bouquetItem = null;
        this.storedPrice = storedPrice;
    }

    public String getName() { return name; }
    public long getTotalPrice() { return bouquetItem == null ? storedPrice : bouquetItem.getSubtotal(); }
    public String getSummary() { return name + ": BDT " + getTotalPrice(); }
}
