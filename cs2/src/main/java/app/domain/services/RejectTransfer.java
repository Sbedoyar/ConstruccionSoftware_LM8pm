package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.OperationLogPort;
import app.domain.ports.TransferPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RejectTransfer {

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    @Autowired
    public RejectTransfer(TransferPort transferPort, UserPort userPort, OperationLogPort operationLogPort) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    @Transactional
    public void rejectTransfer(int transferId, String userIdentification) throws BusinessException {

        // Validación general:
        // El ID de la transferencia debe ser válido.
        if (transferId <= 0) {
            throw new BusinessException("El ID de la transferencia es obligatorio y debe ser mayor que cero");
        }

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca la transferencia.
        Transfer transfer = transferPort.findByTransferId(transferId);
        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        // Se busca el usuario que realiza el rechazo.
        User user = userPort.findByIdentificationNumber(userIdentification);
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // RN-32 / RN-33:
        // Solo el supervisor de empresa puede aprobar o rechazar transferencias.
        validateSupervisorRole(user);

        // RN-32:
        // El supervisor solo puede rechazar transferencias de su empresa.
        validateCompanyOwnership(user, transfer.getSourceAccount());

        // RN-33 / RN-AD15:
        // Solo se puede rechazar una transferencia en espera de aprobación.
        validatePendingApprovalStatus(transfer);

        // RN-17:
        // Si la transferencia ya está vencida por tiempo, no debe rechazarse manualmente.
        validateNotExpired(transfer);

        // RN-AD15:
        // Si se rechaza, el estado final debe ser REJECTED.
        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setReviewedBy(user);
        transfer.setReviewDate(LocalDateTime.now());

        transferPort.update(transfer);

        // RN-20:
        // Registrar el rechazo en la bitácora.
        registerRejectedTransferLog(user, transfer);
    }

    private void validateSupervisorRole(User user) {

        // RN-32 / RN-33:
        // Solo el supervisor de empresa puede rechazar transferencias pendientes.
        if (user.getSystemRole() != RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("Solo un supervisor de empresa puede rechazar transferencias");
        }
    }

    private void validateCompanyOwnership(User user, BankAccount sourceAccount) {

        // RN-32:
        // El supervisor solo puede operar sobre productos de su empresa.
        if (sourceAccount == null || sourceAccount.getOwner() == null || user.getCustomer() == null) {
            throw new BusinessException("No fue posible validar la empresa asociada a la transferencia");
        }

        if (!user.getCustomer().getIdentificationNumber()
                .equals(sourceAccount.getOwner().getIdentificationNumber())) {
            throw new BusinessException("El supervisor solo puede rechazar transferencias de su empresa");
        }
    }

    private void validatePendingApprovalStatus(Transfer transfer) {

        // RN-33 / RN-AD15:
        // Solo se puede rechazar una transferencia en espera de aprobación.
        if (transfer.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new BusinessException("Solo se puede rechazar una transferencia en espera de aprobación");
        }
    }

    private void validateNotExpired(Transfer transfer) {

        // RN-17:
        // Si la transferencia ya superó su fecha límite, debe pasar a vencida
        // y no seguir por el flujo manual de rechazo.
        if (transfer.getExpirationDate() != null && LocalDateTime.now().isAfter(transfer.getExpirationDate())) {
            throw new BusinessException("La transferencia ya venció y no puede ser rechazada manualmente");
        }
    }

    private void registerRejectedTransferLog(User user, Transfer transfer) {

        // RN-20:
        // Registrar el rechazo de la transferencia en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_REJECTED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(transfer.getSourceAccount().getAccountNumber());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("transferId", transfer.getTransferId());
        detailData.put("sourceAccount", transfer.getSourceAccount().getAccountNumber());
        detailData.put("targetAccount", getTargetAccountNumber(transfer));
        detailData.put("transferType", transfer.getTransferType() != null ? transfer.getTransferType().name() : null);
        detailData.put("amount", transfer.getAmount());
        detailData.put("previousStatus", TransferStatus.PENDING_APPROVAL.name());
        detailData.put("newStatus", TransferStatus.REJECTED.name());

        operationLog.setDetailData(detailData);
        operationLogPort.save(operationLog);
    }

    private String getTargetAccountNumber(Transfer transfer) {
        return transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null;
    }
}