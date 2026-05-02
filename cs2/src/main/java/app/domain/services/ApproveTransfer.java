package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApproveTransfer {

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final ExecuteTransfer executeTransfer;
    private final OperationLogPort operationLogPort;

    @Autowired
    public ApproveTransfer(TransferPort transferPort,
                           UserPort userPort,
                           ExecuteTransfer executeTransfer,
                           OperationLogPort operationLogPort) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.executeTransfer = executeTransfer;
        this.operationLogPort = operationLogPort;
    }

    @Transactional
    public void approveTransfer(int transferId, String userIdentification) throws BusinessException {

        // Validación general:
        // El ID de la transferencia debe ser válido.
        if (transferId <= 0) {
            throw new BusinessException("El ID de la transferencia es obligatorio y debe ser mayor que cero");
        }

        // Validación general:
        // La identificación del usuario aprobador es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca la transferencia.
        Transfer transfer = transferPort.findByTransferId(transferId);
        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        // Se busca el usuario aprobador.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);

        // RN-32 / RN-33:
        // El supervisor de empresa es el único rol fuera del banco
        // con capacidad de aprobar transferencias.
        validateSupervisorRole(user);

        // RN-32:
        // El supervisor solo puede aprobar transferencias de su empresa.
        validateCompanyOwnership(user, transfer.getSourceAccount());

        // RN-33 / RN-AD14:
        // Solo se puede aprobar una transferencia en espera de aprobación.
        validatePendingApprovalStatus(transfer);

        // RN-17:
        // Una transferencia vencida ya no puede ser aprobada manualmente.
        validateNotExpired(transfer);

        // Se registra quién aprobó y cuándo lo hizo.
        transfer.setReviewedBy(user);
        transfer.setReviewDate(LocalDateTime.now());

        // RN-AD14:
        // Antes de ejecutar, la transferencia pasa por estado APPROVED.
        transfer.setStatus(TransferStatus.APPROVED);
        transferPort.update(transfer);

        // RN-20:
        // Registrar la aprobación en la bitácora.
        registerApprovedTransferLog(user, transfer);

        // RN-AD14:
        // Si se aprueba, la transferencia debe ejecutarse.
        executeTransfer.executeTransfer(transfer, user);
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para realizar esta operación");
        }
    }

    private void validateSupervisorRole(User user) {

        // RN-32 / RN-33:
        // Solo el supervisor de empresa puede aprobar transferencias pendientes.
        if (user.getSystemRole() != RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("Solo un supervisor de empresa puede aprobar transferencias");
        }
    }

    private void validateCompanyOwnership(User user, BankAccount sourceAccount) {

        // RN-32:
        // El supervisor solo puede operar sobre productos de su empresa.
        if (user.getCustomer() == null || sourceAccount == null || sourceAccount.getOwner() == null ||
            !user.getCustomer().getIdentificationNumber().equals(sourceAccount.getOwner().getIdentificationNumber())) {
            throw new BusinessException("El supervisor solo puede aprobar transferencias de su empresa");
        }
    }

    private void validatePendingApprovalStatus(Transfer transfer) {

        // RN-33 / RN-AD14:
        // Solo se puede aprobar una transferencia en espera de aprobación.
        if (transfer.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new BusinessException("Solo se puede aprobar una transferencia en espera de aprobación");
        }
    }

    private void validateNotExpired(Transfer transfer) {

        // RN-17:
        // Si la transferencia ya superó su fecha límite, debe pasar a vencida
        // y no puede seguir en el flujo manual de aprobación.
        if (transfer.getExpirationDate() != null && LocalDateTime.now().isAfter(transfer.getExpirationDate())) {
            throw new BusinessException("La transferencia ya venció y no puede ser aprobada");
        }
    }

    private void registerApprovedTransferLog(User user, Transfer transfer) {

        // RN-20:
        // Registrar la aprobación de la transferencia en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_APPROVED);
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
        detailData.put("newStatus", TransferStatus.APPROVED.name());

        operationLog.setDetailData(detailData);
        operationLogPort.save(operationLog);
    }

    private String getTargetAccountNumber(Transfer transfer) {
        return transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null;
    }
}
