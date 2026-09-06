package com.everbloom.pricing;

public class LoyaltyPricingStrategy implements PricingStrategy {

    @Override
    public String getName() {
        return "Loyalty Pricing";
    }

    @Override
    public long calculateTotal(long subtotal) {
        validateSubtotal(subtotal);
        long discount = subtotal / 10;
        return subtotal - discount;
    }

    private void validateSubtotal(long subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative.");
        }
    }
}
