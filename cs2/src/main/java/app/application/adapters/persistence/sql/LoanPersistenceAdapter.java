package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.LoanEntity;
import app.application.adapters.persistence.sql.repositories.LoanRepository;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.LoanType;
import app.domain.models.enums.ProductCategory;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import app.domain.ports.out.LoanPort;
import org.springframework.stereotype.Service;

@Service
public class LoanPersistenceAdapter implements LoanPort {

    private final LoanRepository repository;

    public LoanPersistenceAdapter(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan findByLoanId(String loanId) {
        LoanEntity entity = repository.findByLoanId(loanId);
        return toModel(entity);
    }

    @Override
    public void save(Loan loan) {
        repository.save(toEntity(loan));
    }

    @Override
    public void update(Loan loan) {
        repository.save(toEntity(loan));
    }

    private LoanEntity toEntity(Loan loan) {
        LoanEntity entity = new LoanEntity();

        entity.setLoanId(loan.getLoanId());

        if (loan.getLoanType() != null) {
            entity.setLoanType(loan.getLoanType().name());
        }

        entity.setRequestedAmount(loan.getRequestedAmount());
        entity.setApprovedAmount(loan.getApprovedAmount());
        entity.setInterestRate(loan.getInterestRate());
        entity.setTermMonths(loan.getTermMonths());

        if (loan.getLoanStatus() != null) {
            entity.setLoanStatus(loan.getLoanStatus().name());
        }

        if (loan.getOwner() != null) {
            entity.setOwnerIdentification(loan.getOwner().getIdentificationNumber());
        }

        if (loan.getCreatedBy() != null) {
            entity.setCreatedByIdentification(loan.getCreatedBy().getIdentificationNumber());
        }

        entity.setCreationDate(loan.getCreationDate());

        if (loan.getReviewedBy() != null) {
            entity.setReviewedByIdentification(loan.getReviewedBy().getIdentificationNumber());
        }

        entity.setReviewDate(loan.getReviewDate());
        entity.setDisbursementDate(loan.getDisbursementDate());

        if (loan.getDisbursementAccount() != null) {
            entity.setDisbursementAccountNumber(loan.getDisbursementAccount().getAccountNumber());
        }

        if (loan.getCatalog() != null) {
            entity.setProductCode(loan.getCatalog().getProductCode());
            entity.setProductName(loan.getCatalog().getProductName());
            entity.setProductDescription(loan.getCatalog().getDescription());

            if (loan.getCatalog().getCategory() != null) {
                entity.setProductCategory(loan.getCatalog().getCategory().name());
            }

            entity.setRequiresApproval(loan.getCatalog().isRequiresApproval());
            entity.setCatalogActive(loan.getCatalog().isActive());
        }

        return entity;
    }

    private Loan toModel(LoanEntity entity) {
        if (entity == null) {
            return null;
        }

        Loan loan = new Loan();

        loan.setLoanId(entity.getLoanId());

        if (entity.getLoanType() != null) {
            loan.setLoanType(LoanType.valueOf(entity.getLoanType()));
        }

        loan.setRequestedAmount(entity.getRequestedAmount());
        loan.setApprovedAmount(entity.getApprovedAmount());
        loan.setInterestRate(entity.getInterestRate());
        loan.setTermMonths(entity.getTermMonths());

        if (entity.getLoanStatus() != null) {
            loan.setLoanStatus(LoanStatus.valueOf(entity.getLoanStatus()));
        }

        IndividualCustomer owner = new IndividualCustomer();
        owner.setIdentificationNumber(entity.getOwnerIdentification());
        loan.setOwner(owner);

        User createdBy = new User();
        createdBy.setIdentificationNumber(entity.getCreatedByIdentification());
        loan.setCreatedBy(createdBy);

        loan.setCreationDate(entity.getCreationDate());

        if (entity.getReviewedByIdentification() != null) {
            User reviewedBy = new User();
            reviewedBy.setIdentificationNumber(entity.getReviewedByIdentification());
            loan.setReviewedBy(reviewedBy);
        }

        loan.setReviewDate(entity.getReviewDate());
        loan.setDisbursementDate(entity.getDisbursementDate());

        if (entity.getDisbursementAccountNumber() != null) {
            BankAccount disbursementAccount = new BankAccount();
            disbursementAccount.setAccountNumber(entity.getDisbursementAccountNumber());
            loan.setDisbursementAccount(disbursementAccount);
        }

        BankProductCatalog catalog = new BankProductCatalog();
        catalog.setProductCode(entity.getProductCode());
        catalog.setProductName(entity.getProductName());
        catalog.setDescription(entity.getProductDescription());

        if (entity.getProductCategory() != null) {
            catalog.setCategory(ProductCategory.valueOf(entity.getProductCategory()));
        }

        catalog.setRequiresApproval(entity.isRequiresApproval());
        catalog.setActive(entity.isCatalogActive());

        loan.setCatalog(catalog);

        return loan;
    }
}