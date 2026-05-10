package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankProductCatalog;

import java.util.List;

public interface BankProductCatalogUseCase {

    void createCatalog(BankProductCatalog catalog) throws BusinessException;

    List<BankProductCatalog> findAll();
}