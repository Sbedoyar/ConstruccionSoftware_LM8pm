package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.ports.out.BankProductCatalogPort;
import org.springframework.stereotype.Service;

@Service
public class CreateBankProductCatalog {

    private final BankProductCatalogPort bankProductCatalogPort;

    public CreateBankProductCatalog(BankProductCatalogPort bankProductCatalogPort) {
        this.bankProductCatalogPort = bankProductCatalogPort;
    }

    public void createCatalog(BankProductCatalog catalog) throws BusinessException {

        if (catalog == null) {
            throw new BusinessException("El producto del catálogo es obligatorio");
        }

        if (catalog.getProductCode() == null || catalog.getProductCode().trim().isEmpty()) {
            throw new BusinessException("El código del producto es obligatorio");
        }

        if (catalog.getProductName() == null || catalog.getProductName().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }

        if (catalog.getCategory() == null) {
            throw new BusinessException("La categoría del producto es obligatoria");
        }

        BankProductCatalog existingCatalog =
                bankProductCatalogPort.findByProductCode(catalog.getProductCode().trim());

        if (existingCatalog != null) {
            throw new BusinessException("Ya existe un producto con ese código");
        }

        catalog.setProductCode(catalog.getProductCode().trim());
        catalog.setProductName(catalog.getProductName().trim());

        if (catalog.getDescription() != null) {
            catalog.setDescription(catalog.getDescription().trim());
        }

        catalog.setActive(true);

        bankProductCatalogPort.save(catalog);
    }
}