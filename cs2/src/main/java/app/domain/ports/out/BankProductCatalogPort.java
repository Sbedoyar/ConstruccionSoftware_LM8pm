package app.domain.ports.out;

import app.domain.models.bankingProduct.BankProductCatalog;

import java.util.List;

public interface BankProductCatalogPort {

    BankProductCatalog findByProductCode(String productCode);

    void save(BankProductCatalog catalog);

    List<BankProductCatalog> findAll();
}