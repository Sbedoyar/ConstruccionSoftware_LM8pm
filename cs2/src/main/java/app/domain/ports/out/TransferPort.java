package app.domain.ports.out;

import app.domain.models.transfer.Transfer;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferPort {

    Transfer findByTransferId(int transferId);

    List<Transfer> findExpiredPendingTransfers(LocalDateTime now);

    void save(Transfer transfer);

    void update(Transfer transfer);
}