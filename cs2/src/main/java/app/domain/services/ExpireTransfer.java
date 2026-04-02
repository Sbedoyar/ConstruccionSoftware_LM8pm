package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.transfer.Transfer;
import app.domain.ports.OperationLogPort;
import app.domain.ports.TransferPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExpireTransfer {

    private final TransferPort transferPort;
    private final OperationLogPort operationLogPort;

    @Autowired
    public ExpireTransfer(TransferPort transferPort, OperationLogPort operationLogPort) {
        this.transferPort = transferPort;
        this.operationLogPort = operationLogPort;
    }

    public void expireTransfer(int transferId) throws BusinessException {

        // Validación general:
        // El ID de la transferencia debe ser válido.
        if (transferId <= 0) {
            throw new BusinessException("El ID de la transferencia es obligatorio y debe ser mayor que cero");
        }

        // Se busca la transferencia.
        Transfer transfer = transferPort.findByTransferId(transferId);
        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        // RN-17:
        // Solo se puede vencer una transferencia que esté en espera de aprobación.
        validatePendingApprovalStatus(transfer);

        // RN-17:
        // La transferencia debe haber superado su tiempo límite de aprobación.
        validateExpirationTime(transfer);

        // RN-17 / RN-AD16:
        // Si la transferencia vence, cambia a EXPIRED y no se mueve dinero.
        transfer.setStatus(TransferStatus.EXPIRED);

        // Se actualiza la transferencia.
        transferPort.update(transfer);

        // RN-20:
        // Registrar el vencimiento en la bitácora.
        registerExpiredTransferLog(transfer);
    }

    private void validatePendingApprovalStatus(Transfer transfer) {

        // RN-17:
        // Solo se puede vencer una transferencia en espera de aprobación.
        if (transfer.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new BusinessException("Solo se puede vencer una transferencia en espera de aprobación");
        }
    }

    private void validateExpirationTime(Transfer transfer) {

        // RN-17:
        // La transferencia debe tener una fecha de vencimiento definida.
        if (transfer.getExpirationDate() == null) {
            throw new BusinessException("La transferencia no tiene una fecha de vencimiento definida");
        }

        // RN-17:
        // Debe haber superado el tiempo límite para pasar a vencida.
        if (!LocalDateTime.now().isAfter(transfer.getExpirationDate())) {
            throw new BusinessException("La transferencia todavía no ha superado el tiempo límite de aprobación");
        }
    }

    private void registerExpiredTransferLog(Transfer transfer) {

        // RN-20:
        // Registrar el vencimiento de la transferencia en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_EXPIRED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(transfer.getCreatedBy());
        operationLog.setUserRole(
            transfer.getCreatedBy() != null ? transfer.getCreatedBy().getSystemRole() : null
        );
        operationLog.setAffectedProductId(String.valueOf(transfer.getTransferId()));

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("transferId", transfer.getTransferId());
        detailData.put("sourceAccount", transfer.getSourceAccount().getAccountNumber());
        detailData.put("targetAccount", transfer.getTargetAccount().getAccountNumber());
        detailData.put("amount", transfer.getAmount());
        detailData.put("previousStatus", TransferStatus.PENDING_APPROVAL.name());
        detailData.put("newStatus", TransferStatus.EXPIRED.name());
        detailData.put("expirationDate", transfer.getExpirationDate());
        detailData.put("reason", "Expired due to lack of approval within the established time");

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}