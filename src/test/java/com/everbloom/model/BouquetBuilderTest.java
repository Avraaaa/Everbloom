package com.everbloom.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BouquetBuilderTest {

    @Test
    void buildsConfiguredBouquet() {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 12, true);
        Flower lily = new Flower(2, "Lily", "White", 3500, 8, true);

        Bouquet bouquet = new BouquetBuilder()
                .forCustomer(customer)
                .forOccasion("Anniversary")
                .withWrapping("Premium paper")
                .withMessage("Happy anniversary")
                .addFlower(rose, 6)
                .addFlower(lily, 2)
                .build();

        assertEquals(customer, bouquet.getCustomer());
        assertEquals("Anniversary", bouquet.getOccasion());
        assertEquals("Premium paper", bouquet.getWrappingStyle());
        assertEquals("Happy anniversary", bouquet.getMessage());
        assertEquals(2, bouquet.getFlowers().size());
        assertEquals(25000, bouquet.getFlowerSubtotal());
    }

    @Test
    void rejectsIncompleteOrUnavailableConstruction() {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 4, true);

        assertThrows(IllegalArgumentException.class, () -> new BouquetBuilder()
                .forCustomer(customer)
                .forOccasion("Birthday")
                .build());
        assertThrows(IllegalArgumentException.class, () -> new BouquetBuilder()
                .forCustomer(customer)
                .forOccasion("Birthday")
                .addFlower(rose, 5));
    }

    @Test
    void keepsSeparateBuildsIndependent() {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 12, true);
        Flower lily = new Flower(2, "Lily", "White", 3500, 8, true);
        BouquetBuilder builder = new BouquetBuilder()
                .forCustomer(customer)
                .forOccasion("Thank You")
                .addFlower(rose, 3);

        Bouquet firstBouquet = builder.build();
        builder.removeFlower(rose);
        builder.addFlower(lily, 2);
        Bouquet secondBouquet = builder.build();

        assertEquals(1, firstBouquet.getFlowers().size());
        assertEquals("Rose", firstBouquet.getFlowers().getFirst().getFlower().getName());
        assertEquals(3, firstBouquet.getFlowers().getFirst().getQuantity());
        assertEquals("Lily", secondBouquet.getFlowers().getFirst().getFlower().getName());
        assertEquals(2, secondBouquet.getFlowers().getFirst().getQuantity());
    }
}
