package app.domain.ports.out;

import app.domain.models.enums.TransferStatus;
import app.domain.models.transfer.Transfer;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferPort {

    Transfer findByTransferId(int transferId);

    void save(Transfer transfer);

    void update(Transfer transfer);

    List<Transfer> findExpiredPendingTransfers(LocalDateTime now);

    List<Transfer> findByStatus(TransferStatus status);

    List<Transfer> findByCreatedByIdentification(String createdByIdentification);
}