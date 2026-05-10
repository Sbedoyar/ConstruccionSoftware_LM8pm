package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.BankProductCatalogEntity;
import app.application.adapters.persistence.sql.repositories.BankProductCatalogRepository;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.enums.ProductCategory;
import app.domain.ports.out.BankProductCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankProductCatalogPersistenceAdapter implements BankProductCatalogPort {

    private final BankProductCatalogRepository repository;

    public BankProductCatalogPersistenceAdapter(BankProductCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankProductCatalog findByProductCode(String productCode) {
        BankProductCatalogEntity entity = repository.findByProductCode(productCode);
        return toModel(entity);
    }

    @Override
    public void save(BankProductCatalog catalog) {
        repository.save(toEntity(catalog));
    }

    @Override
    public List<BankProductCatalog> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toModel)
                .toList();
    }

    private BankProductCatalogEntity toEntity(BankProductCatalog catalog) {
        BankProductCatalogEntity entity = new BankProductCatalogEntity();

        entity.setProductCode(catalog.getProductCode());
        entity.setProductName(catalog.getProductName());
        entity.setDescription(catalog.getDescription());

        if (catalog.getCategory() != null) {
            entity.setCategory(catalog.getCategory().name());
        }

        entity.setRequiresApproval(catalog.isRequiresApproval());
        entity.setActive(catalog.isActive());

        return entity;
    }

    private BankProductCatalog toModel(BankProductCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        BankProductCatalog catalog = new BankProductCatalog();

        catalog.setProductCode(entity.getProductCode());
        catalog.setProductName(entity.getProductName());
        catalog.setDescription(entity.getDescription());

        if (entity.getCategory() != null) {
            catalog.setCategory(ProductCategory.valueOf(entity.getCategory()));
        }

        catalog.setRequiresApproval(entity.isRequiresApproval());
        catalog.setActive(entity.isActive());

        return catalog;
    }
}