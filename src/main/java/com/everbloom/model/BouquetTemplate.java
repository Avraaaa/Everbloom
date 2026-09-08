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
        this.name = normalize(name);
        this.occasion = normalize(occasion);
        this.wrappingStyle = normalize(wrappingStyle);
        this.message = normalize(message);
        this.flowers = new ArrayList<>(flowers);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getOccasion() { return occasion; }
    public String getWrappingStyle() { return wrappingStyle; }
    public String getMessage() { return message; }
    public List<BouquetFlower> getFlowers() { return List.copyOf(flowers); }

    public String getFlowerSummary() {
        StringBuilder summary = new StringBuilder();
        for (BouquetFlower flower : flowers) {
            if (!summary.isEmpty()) summary.append(", ");
            summary.append(flower.getQuantity()).append(" × ").append(flower.getFlower().getName());
        }
        return summary.toString();
    }

    public BouquetBuilder copyToBuilder() {
        BouquetBuilder builder = new BouquetBuilder().forOccasion(occasion).withWrapping(wrappingStyle).withMessage(message);
        for (BouquetFlower line : flowers) {
            Flower flower = line.getFlower();
            builder.addFlower(new Flower(flower.getId(), flower.getName(), flower.getColor(), flower.getUnitPrice(), flower.getStockQuantity(), flower.isActive()), line.getQuantity());
        }
        return builder;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
