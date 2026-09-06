package com.everbloom.service;

import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.repository.BouquetTemplateRepository;
import java.sql.SQLException;
import java.util.List;

public class BouquetTemplateService {
    private final BouquetTemplateRepository repository;
    public BouquetTemplateService(BouquetTemplateRepository repository) { this.repository = repository; }
    public List<BouquetTemplate> findAll() throws SQLException { return repository.findAll(); }
    public BouquetBuilder copyToBuilder(BouquetTemplate template) {
        if (template == null) { throw new IllegalArgumentException("Select a bouquet template."); }
        return template.copyToBuilder();
    }
    public boolean delete(long id) throws SQLException { return repository.delete(id); }
}
