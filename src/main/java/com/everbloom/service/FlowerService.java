package com.everbloom.service;

import com.everbloom.model.Flower;
import com.everbloom.repository.FlowerRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FlowerService {

    private final FlowerRepository flowerRepository;

    public FlowerService(FlowerRepository flowerRepository) {
        this.flowerRepository = flowerRepository;
    }

    public Flower create(Flower flower) throws SQLException {
        validate(flower);
        normalize(flower);
        return flowerRepository.create(flower);
    }

    public Optional<Flower> findById(long id) throws SQLException {
        validateId(id);
        return flowerRepository.findById(id);
    }

    public List<Flower> findAll() throws SQLException {
        return flowerRepository.findAll();
    }

    public List<Flower> search(String searchText) throws SQLException {
        String normalizedSearch = searchText == null ? "" : searchText.trim();
        if (normalizedSearch.isEmpty()) {
            return findAll();
        }
        return flowerRepository.search(normalizedSearch);
    }

    public boolean update(Flower flower) throws SQLException {
        validate(flower);
        validateId(flower.getId());
        normalize(flower);
        return flowerRepository.update(flower);
    }

    public boolean delete(long id) throws SQLException {
        validateId(id);
        return flowerRepository.delete(id);
    }

    private void validate(Flower flower) {
        if (flower == null) {
            throw new IllegalArgumentException("Flower is required.");
        }
        if (flower.getName() == null || flower.getName().isBlank()) {
            throw new IllegalArgumentException("Flower name is required.");
        }
        if (flower.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Flower price cannot be negative.");
        }
        if (flower.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Flower stock quantity cannot be negative.");
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Flower ID must be positive.");
        }
    }

    private void normalize(Flower flower) {
        flower.setName(flower.getName().trim());
        flower.setColor(normalizeOptionalText(flower.getColor()));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
