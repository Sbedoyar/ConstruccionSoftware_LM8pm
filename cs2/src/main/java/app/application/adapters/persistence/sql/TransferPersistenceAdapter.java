package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.TransferEntity;
import app.application.adapters.persistence.sql.repositories.TransferRepository;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.TransferPort;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TransferPersistenceAdapter implements TransferPort {

    private final TransferRepository repository;
    private final AccountPort accountPort;

    public TransferPersistenceAdapter(TransferRepository repository,
                                      AccountPort accountPort) {
        this.repository = repository;
        this.accountPort = accountPort;
    }

    @Override
    public Transfer findByTransferId(int transferId) {
        TransferEntity entity = repository.findByTransferId(transferId);
        return toModel(entity);
    }

    @Override
    public List<Transfer> findExpiredPendingTransfers(LocalDateTime now) {
        return repository.findByStatusAndExpirationDateBefore(
                        TransferStatus.PENDING_APPROVAL.name(),
                        now
                )
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public void save(Transfer transfer) {
        TransferEntity savedEntity = repository.save(toEntity(transfer));
        transfer.setTransferId(savedEntity.getTransferId());
    }

    @Override
    public void update(Transfer transfer) {
        repository.save(toEntity(transfer));
    }

    private TransferEntity toEntity(Transfer transfer) {
        TransferEntity entity = new TransferEntity();

        entity.setTransferId(transfer.getTransferId());

        if (transfer.getSourceAccount() != null) {
            entity.setSourceAccountNumber(transfer.getSourceAccount().getAccountNumber());
        }

        if (transfer.getTargetAccount() != null) {
            entity.setTargetAccountNumber(transfer.getTargetAccount().getAccountNumber());
        }

        entity.setAmount(transfer.getAmount());
        entity.setExpirationDate(transfer.getExpirationDate());

        if (transfer.getStatus() != null) {
            entity.setStatus(transfer.getStatus().name());
        }

        if (transfer.getCreatedBy() != null) {
            entity.setCreatedByIdentification(transfer.getCreatedBy().getIdentificationNumber());
        }

        entity.setCreationDate(transfer.getCreationDate());

        if (transfer.getReviewedBy() != null) {
            entity.setReviewedByIdentification(transfer.getReviewedBy().getIdentificationNumber());
        }

        entity.setReviewDate(transfer.getReviewDate());

        if (transfer.getTransferType() != null) {
            entity.setTransferType(transfer.getTransferType().name());
        }

        return entity;
    }

    private Transfer toModel(TransferEntity entity) {
        if (entity == null) {
            return null;
        }

        Transfer transfer = new Transfer();

        transfer.setTransferId(entity.getTransferId());

        BankAccount sourceAccount = accountPort.findByAccountNumber(entity.getSourceAccountNumber());
        if (sourceAccount == null) {
            sourceAccount = new BankAccount();
            sourceAccount.setAccountNumber(entity.getSourceAccountNumber());
        }
        transfer.setSourceAccount(sourceAccount);

        if (entity.getTargetAccountNumber() != null) {
            BankAccount targetAccount = accountPort.findByAccountNumber(entity.getTargetAccountNumber());
            if (targetAccount == null) {
                targetAccount = new BankAccount();
                targetAccount.setAccountNumber(entity.getTargetAccountNumber());
            }
            transfer.setTargetAccount(targetAccount);
        }

        transfer.setAmount(entity.getAmount());
        transfer.setExpirationDate(entity.getExpirationDate());

        if (entity.getStatus() != null) {
            transfer.setStatus(TransferStatus.valueOf(entity.getStatus()));
        }

        User createdBy = new User();
        createdBy.setIdentificationNumber(entity.getCreatedByIdentification());
        transfer.setCreatedBy(createdBy);

        transfer.setCreationDate(entity.getCreationDate());

        if (entity.getReviewedByIdentification() != null) {
            User reviewedBy = new User();
            reviewedBy.setIdentificationNumber(entity.getReviewedByIdentification());
            transfer.setReviewedBy(reviewedBy);
        }

        transfer.setReviewDate(entity.getReviewDate());

        if (entity.getTransferType() != null) {
            transfer.setTransferType(TransferType.valueOf(entity.getTransferType()));
        }

        return transfer;
    }

}