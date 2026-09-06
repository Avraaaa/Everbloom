package com.everbloom.model;

import java.util.List;

public class Bouquet {

    private final Customer customer;
    private final String occasion;
    private final List<BouquetFlower> flowers;
    private final String wrappingStyle;
    private final String message;

    Bouquet(Customer customer, String occasion, List<BouquetFlower> flowers, String wrappingStyle, String message) {
        this.customer = customer;
        this.occasion = occasion;
        this.flowers = List.copyOf(flowers);
        this.wrappingStyle = wrappingStyle;
        this.message = message;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getOccasion() {
        return occasion;
    }

    public List<BouquetFlower> getFlowers() {
        return flowers;
    }

    public String getWrappingStyle() {
        return wrappingStyle;
    }

    public String getMessage() {
        return message;
    }

    public long getFlowerSubtotal() {
        long subtotal = 0;
        for (BouquetFlower bouquetFlower : flowers) {
            subtotal += bouquetFlower.getLineTotal();
        }
        return subtotal;
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Customer: ").append(customer.getFullName()).append("\n");
        summary.append("Occasion: ").append(occasion).append("\n\n");
        summary.append("Flowers\n");
        for (BouquetFlower bouquetFlower : flowers) {
            summary.append("- ")
                    .append(bouquetFlower.getQuantity())
                    .append(" × ")
                    .append(bouquetFlower.getFlower().getName())
                    .append("\n");
        }
        summary.append("\nWrapping: ").append(wrappingStyle == null ? "None" : wrappingStyle).append("\n");
        if (message != null) {
            summary.append("Message: ").append(message).append("\n");
        }
        summary.append("\nFlower subtotal: BDT ").append(getFlowerSubtotal());
        return summary.toString();
    }
}
