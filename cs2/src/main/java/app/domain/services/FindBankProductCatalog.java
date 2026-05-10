package app.domain.services;

import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.ports.out.BankProductCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindBankProductCatalog {

    private final BankProductCatalogPort bankProductCatalogPort;

    public FindBankProductCatalog(BankProductCatalogPort bankProductCatalogPort) {
        this.bankProductCatalogPort = bankProductCatalogPort;
    }

    public List<BankProductCatalog> findAll() {
        return bankProductCatalogPort.findAll();
    }
}