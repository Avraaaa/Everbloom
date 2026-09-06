package com.everbloom.pricing;

public interface PricingStrategy {

    String getName();

    long calculateTotal(long subtotal);
}
