package com.everbloom.model;

public class ExtraDecorator extends BouquetDecorator {

    private final Extra extra;

    public ExtraDecorator(BouquetItem bouquetItem, Extra extra) {
        super(bouquetItem);
        validateExtra(extra);
        this.extra = extra;
    }

    @Override
    public String getDescription() {
        return bouquetItem.getDescription() + " + " + extra.getName();
    }

    @Override
    public long getSubtotal() {
        return bouquetItem.getSubtotal() + extra.getUnitPrice();
    }

    private void validateExtra(Extra extra) {
        if (extra == null) {
            throw new IllegalArgumentException("Extra is required.");
        }
        if (extra.getName() == null || extra.getName().isBlank()) {
            throw new IllegalArgumentException("Extra name is required.");
        }
        if (!extra.isActive()) {
            throw new IllegalArgumentException("Only active extras can be added to a bouquet.");
        }
        if (extra.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Extra price cannot be negative.");
        }
    }
}
