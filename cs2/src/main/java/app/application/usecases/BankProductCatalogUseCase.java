package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.services.CreateBankProductCatalog;
import app.domain.services.FindBankProductCatalog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankProductCatalogUseCase implements app.domain.ports.in.BankProductCatalogUseCase {

    private final CreateBankProductCatalog createBankProductCatalog;
    private final FindBankProductCatalog findBankProductCatalog;

    public BankProductCatalogUseCase(CreateBankProductCatalog createBankProductCatalog,
                                     FindBankProductCatalog findBankProductCatalog) {
        this.createBankProductCatalog = createBankProductCatalog;
        this.findBankProductCatalog = findBankProductCatalog;
    }

    @Override
    public void createCatalog(BankProductCatalog catalog) throws BusinessException {
        createBankProductCatalog.createCatalog(catalog);
    }

    @Override
    public List<BankProductCatalog> findAll() {
        return findBankProductCatalog.findAll();
    }
}