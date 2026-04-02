package app.domain.ports;

import java.util.List;

import app.domain.models.transfer.Transfer;

public interface TransferPort {
    // RN14: Validar unicidad
    Transfer findById(int id);

    // RN20: Guardar transferencia
    void save(Transfer transfer);

    // RN17: Buscar transferencias pendientes
    List<Transfer> findPendingTransfers();

}
