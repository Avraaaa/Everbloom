package com.everbloom.service;

import com.everbloom.model.BouquetItem;
import com.everbloom.pricing.LoyaltyPricingStrategy;
import com.everbloom.pricing.StandardPricingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void standardAndLoyaltyPricingProduceDifferentTotals() {
        BouquetItem bouquetItem = new TestBouquetItem(10000);

        assertEquals(10000, pricingService.calculateFinalTotal(bouquetItem, new StandardPricingStrategy()));
        assertEquals(9000, pricingService.calculateFinalTotal(bouquetItem, new LoyaltyPricingStrategy()));
        assertEquals(1000, pricingService.calculateDiscount(bouquetItem, new LoyaltyPricingStrategy()));
    }

    @Test
    void checkoutCalculationChangesWhenTheStrategyChanges() {
        BouquetItem bouquetItem = new TestBouquetItem(6500);

        long standardTotal = pricingService.calculateFinalTotal(bouquetItem, new StandardPricingStrategy());
        long loyaltyTotal = pricingService.calculateFinalTotal(bouquetItem, new LoyaltyPricingStrategy());

        assertEquals(6500, standardTotal);
        assertEquals(5850, loyaltyTotal);
    }

    @Test
    void loyaltyPricingKeepsZeroSubtotalAtZero() {
        BouquetItem bouquetItem = new TestBouquetItem(0);

        assertEquals(0, pricingService.calculateFinalTotal(bouquetItem, new LoyaltyPricingStrategy()));
        assertEquals(0, pricingService.calculateDiscount(bouquetItem, new LoyaltyPricingStrategy()));
    }

    @Test
    void pricesAnEventPackageSubtotalWithTheExistingStrategy() {
        assertEquals(18000, pricingService.calculateFinalTotal(20000, new LoyaltyPricingStrategy()));
        assertEquals(2000, pricingService.calculateDiscount(20000, new LoyaltyPricingStrategy()));
    }

    private static class TestBouquetItem implements BouquetItem {

        private final long subtotal;

        private TestBouquetItem(long subtotal) {
            this.subtotal = subtotal;
        }

        @Override
        public String getDescription() {
            return "Test bouquet";
        }

        @Override
        public long getSubtotal() {
            return subtotal;
        }
    }
}
