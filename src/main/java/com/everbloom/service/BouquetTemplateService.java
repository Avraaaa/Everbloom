package com.everbloom.service;

import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.repository.BouquetTemplateRepository;
import java.sql.SQLException;
import java.util.List;

public class BouquetTemplateService {
    private final BouquetTemplateRepository repository;
    public BouquetTemplateService(BouquetTemplateRepository repository) { this.repository = repository; }
    public List<BouquetTemplate> findAll() throws SQLException { return repository.findAll(); }
    public BouquetTemplate create(BouquetTemplate template) throws SQLException {
        validate(template, false);
        return repository.create(template);
    }
    public boolean update(BouquetTemplate template) throws SQLException {
        validate(template, true);
        return repository.update(template);
    }
    public BouquetBuilder copyToBuilder(BouquetTemplate template) {
        if (template == null) { throw new IllegalArgumentException("Select a bouquet template."); }
        return template.copyToBuilder();
    }
    public boolean delete(long id) throws SQLException { return repository.delete(id); }

    private void validate(BouquetTemplate template, boolean existing) {
        if (template == null) throw new IllegalArgumentException("Bouquet template is required.");
        if (existing && template.getId() <= 0) throw new IllegalArgumentException("Template ID must be positive.");
        if (template.getName() == null) throw new IllegalArgumentException("Template name is required.");
        if (template.getOccasion() == null) throw new IllegalArgumentException("Template occasion is required.");
        if (template.getFlowers().isEmpty()) throw new IllegalArgumentException("Add at least one flower to the template.");
        for (BouquetFlower flower : template.getFlowers()) {
            if (flower.getFlower() == null || flower.getFlower().getId() <= 0 || flower.getQuantity() <= 0) {
                throw new IllegalArgumentException("Template flowers must be saved flowers with positive quantities.");
            }
        }
    }
}
