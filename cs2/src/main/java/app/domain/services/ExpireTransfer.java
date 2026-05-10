package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExpireTransfer {

    private final TransferPort transferPort;
    private final OperationLogPort operationLogPort;
    private final UserPort userPort;

    @Autowired
    public ExpireTransfer(TransferPort transferPort,
                          OperationLogPort operationLogPort,
                          UserPort userPort) {
        this.transferPort = transferPort;
        this.operationLogPort = operationLogPort;
        this.userPort = userPort;
    }

    @Transactional
    public void expireTransfer(int transferId) throws BusinessException {

        if (transferId <= 0) {
            throw new BusinessException("El ID de la transferencia es obligatorio y debe ser mayor que cero");
        }

        Transfer transfer = transferPort.findByTransferId(transferId);
        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        validatePendingApprovalStatus(transfer);
        validateExpirationTime(transfer);

        transfer.setStatus(TransferStatus.EXPIRED);

        transferPort.update(transfer);

        registerExpiredTransferLog(transfer);
    }

    @Transactional
    public void expirePendingTransfers() {
        List<Transfer> expiredTransfers =
                transferPort.findExpiredPendingTransfers(LocalDateTime.now());

        for (Transfer transfer : expiredTransfers) {
            expireTransfer(transfer.getTransferId());
        }
    }

    private void validatePendingApprovalStatus(Transfer transfer) {

        if (transfer.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new BusinessException("Solo se puede vencer una transferencia en espera de aprobación");
        }
    }

    private void validateExpirationTime(Transfer transfer) {

        if (transfer.getExpirationDate() == null) {
            throw new BusinessException("La transferencia no tiene una fecha de vencimiento definida");
        }

        if (!LocalDateTime.now().isAfter(transfer.getExpirationDate())) {
            throw new BusinessException("La transferencia todavía no ha superado el tiempo límite de aprobación");
        }
    }

    private void registerExpiredTransferLog(Transfer transfer) {

        User createdBy = findCompleteCreatedByUser(transfer);

        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_EXPIRED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(createdBy);
        operationLog.setUserRole(createdBy != null ? createdBy.getSystemRole() : null);
        operationLog.setAffectedProductId(
                transfer.getSourceAccount() != null
                        ? transfer.getSourceAccount().getAccountNumber()
                        : null
        );

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("transferId", transfer.getTransferId());
        detailData.put("sourceAccount", getSourceAccountNumber(transfer));
        detailData.put("targetAccount", getTargetAccountNumber(transfer));
        detailData.put("transferType", transfer.getTransferType() != null ? transfer.getTransferType().name() : null);
        detailData.put("amount", transfer.getAmount());
        detailData.put("previousStatus", TransferStatus.PENDING_APPROVAL.name());
        detailData.put("newStatus", TransferStatus.EXPIRED.name());
        detailData.put("expirationDate", transfer.getExpirationDate());
        detailData.put("reason", "Vencida por falta de aprobación en el tiempo establecido");

        operationLog.setDetailData(detailData);
        operationLogPort.save(operationLog);
    }

    private User findCompleteCreatedByUser(Transfer transfer) {

        if (transfer.getCreatedBy() == null ||
                transfer.getCreatedBy().getIdentificationNumber() == null ||
                transfer.getCreatedBy().getIdentificationNumber().trim().isEmpty()) {
            return transfer.getCreatedBy();
        }

        User user = userPort.findByIdentificationNumber(
                transfer.getCreatedBy().getIdentificationNumber().trim()
        );

        if (user == null) {
            return transfer.getCreatedBy();
        }

        return user;
    }

    private String getSourceAccountNumber(Transfer transfer) {
        return transfer.getSourceAccount() != null ? transfer.getSourceAccount().getAccountNumber() : null;
    }

    private String getTargetAccountNumber(Transfer transfer) {
        return transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null;
    }
}