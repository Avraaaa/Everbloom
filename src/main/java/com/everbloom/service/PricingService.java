package com.everbloom.service;

import com.everbloom.model.BouquetItem;
import com.everbloom.pricing.PricingStrategy;

public class PricingService {

    public long calculateFinalTotal(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        validateInputs(bouquetItem, pricingStrategy);
        return pricingStrategy.calculateTotal(bouquetItem.getSubtotal());
    }

    public long calculateDiscount(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        validateInputs(bouquetItem, pricingStrategy);
        long subtotal = bouquetItem.getSubtotal();
        long finalTotal = pricingStrategy.calculateTotal(subtotal);
        return subtotal - finalTotal;
    }

    private void validateInputs(BouquetItem bouquetItem, PricingStrategy pricingStrategy) {
        if (bouquetItem == null) {
            throw new IllegalArgumentException("Bouquet item is required.");
        }
        if (pricingStrategy == null) {
            throw new IllegalArgumentException("Pricing strategy is required.");
        }
    }
}
