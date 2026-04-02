package app.domain.ports;

import app.domain.models.transfer.Transfer;

public interface TransferPort {
    Transfer findByTransferId(int transferId);
    void save(Transfer transfer);
    void update(Transfer transfer);
}
