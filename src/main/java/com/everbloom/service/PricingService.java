package com.everbloom.service;

import com.everbloom.model.BouquetItem;
import com.everbloom.pricing.PricingStrategy;

public class PricingService {

    public long calculateFinalTotal(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        validateInputs(bouquetItem, pricingStrategy);
        return calculateFinalTotal(bouquetItem.getSubtotal(), pricingStrategy);
    }

    public long calculateDiscount(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        validateInputs(bouquetItem, pricingStrategy);
        return calculateDiscount(bouquetItem.getSubtotal(), pricingStrategy);
    }

    public long calculateFinalTotal(long subtotal, PricingStrategy pricingStrategy) {
        validateSubtotal(subtotal, pricingStrategy);
        return pricingStrategy.calculateTotal(subtotal);
    }

    public long calculateDiscount(long subtotal, PricingStrategy pricingStrategy) {
        validateSubtotal(subtotal, pricingStrategy);
        return subtotal - pricingStrategy.calculateTotal(subtotal);
    }

    private void validateInputs(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        if (bouquetItem == null) {
            throw new IllegalArgumentException("Bouquet item is required.");
        }
        if (pricingStrategy == null) {
            throw new IllegalArgumentException("Pricing strategy is required.");
        }
    }

    private void validateSubtotal(long subtotal, PricingStrategy pricingStrategy) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative.");
        }
        if (pricingStrategy == null) {
            throw new IllegalArgumentException("Pricing strategy is required.");
        }
    }
}
