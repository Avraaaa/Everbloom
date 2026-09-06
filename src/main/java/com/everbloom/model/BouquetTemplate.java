package com.everbloom.model;

import java.util.ArrayList;
import java.util.List;

public class BouquetTemplate {
    private long id;
    private final String name;
    private final String occasion;
    private final String wrappingStyle;
    private final String message;
    private final List<BouquetFlower> flowers;

    public BouquetTemplate(long id, String name, String occasion, String wrappingStyle, String message, List<BouquetFlower> flowers) {
        this.id = id;
        this.name = name;
        this.occasion = occasion;
        this.wrappingStyle = wrappingStyle;
        this.message = message;
        this.flowers = new ArrayList<>(flowers);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getOccasion() { return occasion; }
    public String getWrappingStyle() { return wrappingStyle; }
    public String getMessage() { return message; }
    public List<BouquetFlower> getFlowers() { return List.copyOf(flowers); }

    public BouquetBuilder copyToBuilder() {
        BouquetBuilder builder = new BouquetBuilder().forOccasion(occasion).withWrapping(wrappingStyle).withMessage(message);
        for (BouquetFlower line : flowers) {
            Flower flower = line.getFlower();
            builder.addFlower(new Flower(flower.getId(), flower.getName(), flower.getColor(), flower.getUnitPrice(), flower.getStockQuantity(), flower.isActive()), line.getQuantity());
        }
        return builder;
    }
}
