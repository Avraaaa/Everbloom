package com.everbloom.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtraDecoratorTest {

    @Test
    void addsOneExtraToBouquetDescriptionAndSubtotal() {
        Bouquet bouquet = createBouquet();
        Extra card = new Extra(1, "Greeting Card", "Handwritten message", 1200, true);

        BouquetItem bouquetItem = new ExtraDecorator(new BaseBouquet(bouquet), card);

        assertEquals("Birthday bouquet + Greeting Card", bouquetItem.getDescription());
        assertEquals(7200, bouquetItem.getSubtotal());
    }

    @Test
    void stacksDifferentExtrasOnTheSameBouquet() {
        Bouquet bouquet = createBouquet();
        Extra card = new Extra(1, "Greeting Card", "Handwritten message", 1200, true);
        Extra chocolate = new Extra(2, "Chocolate Box", "Assorted chocolates", 6500, true);

        BouquetItem bouquetItem = new ExtraDecorator(
                new ExtraDecorator(new BaseBouquet(bouquet), card),
                chocolate
        );

        assertEquals("Birthday bouquet + Greeting Card + Chocolate Box", bouquetItem.getDescription());
        assertEquals(13700, bouquetItem.getSubtotal());
    }

    private Bouquet createBouquet() {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 12, true);
        return new BouquetBuilder()
                .forCustomer(customer)
                .forOccasion("Birthday")
                .addFlower(rose, 2)
                .build();
    }
}
