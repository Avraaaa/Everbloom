package com.everbloom.service;

import com.everbloom.model.Extra;
import com.everbloom.repository.ExtraRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExtraService {

    private final ExtraRepository extraRepository;

    public ExtraService(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    public Extra create(Extra extra) throws SQLException {
        validate(extra);
        normalize(extra);
        return extraRepository.create(extra);
    }

    public Optional<Extra> findById(long id) throws SQLException {
        validateId(id);
        return extraRepository.findById(id);
    }

    public List<Extra> findAll() throws SQLException {
        return extraRepository.findAll();
    }

    public List<Extra> search(String searchText) throws SQLException {
        String normalizedSearch = searchText == null ? "" : searchText.trim();
        if (normalizedSearch.isEmpty()) {
            return findAll();
        }
        return extraRepository.search(normalizedSearch);
    }

    public boolean update(Extra extra) throws SQLException {
        validate(extra);
        validateId(extra.getId());
        normalize(extra);
        return extraRepository.update(extra);
    }

    public boolean delete(long id) throws SQLException {
        validateId(id);
        return extraRepository.delete(id);
    }

    private void validate(Extra extra) {
        if (extra == null) {
            throw new IllegalArgumentException("Extra is required.");
        }
        if (extra.getName() == null || extra.getName().isBlank()) {
            throw new IllegalArgumentException("Extra name is required.");
        }
        if (extra.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Extra price cannot be negative.");
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Extra ID must be positive.");
        }
    }

    private void normalize(Extra extra) {
        extra.setName(extra.getName().trim());
        extra.setDescription(normalizeOptionalText(extra.getDescription()));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
