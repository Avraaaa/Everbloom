package com.everbloom.pricing;

public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public String getName() {
        return "Standard Pricing";
    }

    @Override
    public long calculateTotal(long subtotal) {
        validateSubtotal(subtotal);
        return subtotal;
    }

    private void validateSubtotal(long subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative.");
        }
    }
}
