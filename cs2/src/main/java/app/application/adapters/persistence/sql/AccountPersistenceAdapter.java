package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.AccountEntity;
import app.application.adapters.persistence.sql.repositories.AccountRepository;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.CurrencyType;
import app.domain.models.enums.ProductCategory;
import app.domain.models.person.IndividualCustomer;
import app.domain.ports.out.AccountPort;
import org.springframework.stereotype.Service;

@Service
public class AccountPersistenceAdapter implements AccountPort {

    private final AccountRepository repository;

    public AccountPersistenceAdapter(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankAccount findByAccountNumber(String accountNumber) {
        AccountEntity entity = repository.findByAccountNumber(accountNumber);
        return toModel(entity);
    }

    @Override
    public void save(BankAccount account) {
        repository.save(toEntity(account));
    }

    @Override
    public void update(BankAccount account) {
        AccountEntity existingEntity = repository.findByAccountNumber(account.getAccountNumber());

        if (existingEntity == null) {
            repository.save(toEntity(account));
            return;
        }

        AccountEntity entity = toEntity(account);
        entity.setId(existingEntity.getId());

        repository.save(entity);
    }

    private AccountEntity toEntity(BankAccount account) {
        AccountEntity entity = new AccountEntity();

        entity.setAccountNumber(account.getAccountNumber());

        if (account.getAccountType() != null) {
            entity.setAccountType(account.getAccountType().name());
        }

        entity.setBalance(account.getBalance());

        if (account.getCurrency() != null) {
            entity.setCurrency(account.getCurrency().name());
        }

        if (account.getAccountStatus() != null) {
            entity.setAccountStatus(account.getAccountStatus().name());
        }

        entity.setOpeningDate(account.getOpeningDate());

        if (account.getOwner() != null) {
            entity.setOwnerIdentification(account.getOwner().getIdentificationNumber());
        }

        if (account.getCatalog() != null) {
            entity.setProductCode(account.getCatalog().getProductCode());
            entity.setProductName(account.getCatalog().getProductName());
            entity.setProductDescription(account.getCatalog().getDescription());

            if (account.getCatalog().getCategory() != null) {
                entity.setProductCategory(account.getCatalog().getCategory().name());
            }

            entity.setRequiresApproval(account.getCatalog().isRequiresApproval());
            entity.setCatalogActive(account.getCatalog().isActive());
        }

        return entity;
    }

    private BankAccount toModel(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        BankAccount account = new BankAccount();

        account.setAccountNumber(entity.getAccountNumber());

        if (entity.getAccountType() != null) {
            account.setAccountType(AccountType.valueOf(entity.getAccountType()));
        }

        account.setBalance(entity.getBalance());

        if (entity.getCurrency() != null) {
            account.setCurrency(CurrencyType.valueOf(entity.getCurrency()));
        }

        if (entity.getAccountStatus() != null) {
            account.setAccountStatus(AccountStatus.valueOf(entity.getAccountStatus()));
        }

        account.setOpeningDate(entity.getOpeningDate());

        IndividualCustomer owner = new IndividualCustomer();
        owner.setIdentificationNumber(entity.getOwnerIdentification());
        account.setOwner(owner);

        BankProductCatalog catalog = new BankProductCatalog();
        catalog.setProductCode(entity.getProductCode());
        catalog.setProductName(entity.getProductName());
        catalog.setDescription(entity.getProductDescription());

        if (entity.getProductCategory() != null) {
            catalog.setCategory(ProductCategory.valueOf(entity.getProductCategory()));
        }

        catalog.setRequiresApproval(entity.isRequiresApproval());
        catalog.setActive(entity.isCatalogActive());

        account.setCatalog(catalog);

        return account;
    }
}