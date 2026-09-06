package com.everbloom.model;

import java.util.ArrayList;
import java.util.List;

public class BouquetBuilder {

    private Customer customer;
    private String occasion;
    private String wrappingStyle;
    private String message;
    private final List<BouquetFlower> selectedFlowers = new ArrayList<>();

    public BouquetBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public BouquetBuilder forOccasion(String occasion) {
        this.occasion = normalizeOptionalText(occasion);
        return this;
    }

    public BouquetBuilder withWrapping(String wrappingStyle) {
        this.wrappingStyle = normalizeOptionalText(wrappingStyle);
        return this;
    }

    public BouquetBuilder withMessage(String message) {
        this.message = normalizeOptionalText(message);
        return this;
    }

    public BouquetBuilder addFlower(Flower flower, int quantity) {
        validateFlowerSelection(flower, quantity);
        int existingIndex = findFlowerIndex(flower);
        if (existingIndex >= 0) {
            BouquetFlower existingFlower = selectedFlowers.get(existingIndex);
            int combinedQuantity = existingFlower.getQuantity() + quantity;
            validateQuantityAgainstStock(flower, combinedQuantity);
            selectedFlowers.set(existingIndex, new BouquetFlower(flower, combinedQuantity));
        } else {
            selectedFlowers.add(new BouquetFlower(flower, quantity));
        }
        return this;
    }

    public boolean removeFlower(Flower flower) {
        int flowerIndex = findFlowerIndex(flower);
        if (flowerIndex < 0) {
            return false;
        }
        selectedFlowers.remove(flowerIndex);
        return true;
    }

    public boolean removeFlowerQuantity(Flower flower, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Flower quantity must be at least one.");
        }
        int flowerIndex = findFlowerIndex(flower);
        if (flowerIndex < 0) {
            return false;
        }
        BouquetFlower selectedFlower = selectedFlowers.get(flowerIndex);
        if (quantity > selectedFlower.getQuantity()) {
            throw new IllegalArgumentException("Cannot remove more flowers than selected.");
        }
        if (quantity == selectedFlower.getQuantity()) {
            selectedFlowers.remove(flowerIndex);
        } else {
            selectedFlowers.set(flowerIndex, new BouquetFlower(flower, selectedFlower.getQuantity() - quantity));
        }
        return true;
    }

    public List<BouquetFlower> getSelectedFlowers() {
        return List.copyOf(selectedFlowers);
    }

    public Bouquet build() {
        validateBouquet();
        return new Bouquet(customer, occasion, selectedFlowers, wrappingStyle, message);
    }

    private void validateBouquet() {
        if (customer == null) {
            throw new IllegalArgumentException("Select a customer for the bouquet.");
        }
        if (occasion == null) {
            throw new IllegalArgumentException("Select an occasion for the bouquet.");
        }
        if (selectedFlowers.isEmpty()) {
            throw new IllegalArgumentException("Add at least one flower to the bouquet.");
        }
        for (BouquetFlower bouquetFlower : selectedFlowers) {
            validateFlowerSelection(bouquetFlower.getFlower(), bouquetFlower.getQuantity());
        }
    }

    private void validateFlowerSelection(Flower flower, int quantity) {
        if (flower == null) {
            throw new IllegalArgumentException("Select a flower to add.");
        }
        if (!flower.isActive()) {
            throw new IllegalArgumentException("Only active flowers can be added to a bouquet.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Flower quantity must be at least one.");
        }
        validateQuantityAgainstStock(flower, quantity);
    }

    private void validateQuantityAgainstStock(Flower flower, int quantity) {
        if (quantity > flower.getStockQuantity()) {
            throw new IllegalArgumentException("Selected quantity exceeds available stock for " + flower.getName() + ".");
        }
    }

    private int findFlowerIndex(Flower flower) {
        for (int index = 0; index < selectedFlowers.size(); index++) {
            Flower selectedFlower = selectedFlowers.get(index).getFlower();
            if (selectedFlower == flower || (flower.getId() > 0 && selectedFlower.getId() == flower.getId())) {
                return index;
            }
        }
        return -1;
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
