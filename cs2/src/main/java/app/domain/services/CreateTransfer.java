package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CreateTransfer {

    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000000");

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final ExecuteTransfer executeTransfer;
    private final OperationLogPort operationLogPort;
    private final AccountPort accountPort;
    private final ValidateAccountOperation validateAccountOperation;

    @Autowired
    public CreateTransfer(TransferPort transferPort,
                          UserPort userPort,
                          ExecuteTransfer executeTransfer,
                          OperationLogPort operationLogPort,
                          AccountPort accountPort, ValidateAccountOperation validateAccountOperation) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.executeTransfer = executeTransfer;
        this.operationLogPort = operationLogPort;
        this.accountPort = accountPort;
        this.validateAccountOperation = validateAccountOperation;
    }

    @Transactional
    public void createTransfer(String userIdentification, Transfer transfer) throws BusinessException {

        // Validación general de entrada.
        if (transfer == null) {
            throw new BusinessException("La transferencia no puede ser null");
        }

        // Validación general:
        // La identificación del usuario creador es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca el usuario creador.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Validación general:
        // El usuario creador debe estar activo.
        validateActiveUser(user);

        // RN-14:
        // El monto debe ser estrictamente mayor que cero.
        validateAmount(transfer.getAmount());

        // Validación general:
        // El tipo de transferencia es obligatorio.
        validateTransferType(transfer);

        // Validación general:
        // La cuenta origen es obligatoria.
        validateSourceAccount(transfer);

        loadSourceAccount(transfer);

        // RN-05 / RN-16:
        // No se permiten operaciones desde cuentas bloqueadas o canceladas.
        validateAccountOperation.validateAccountOperability(transfer.getSourceAccount());

        // Validación general:
        // Si la transferencia es interna, la cuenta destino es obligatoria.
        validateTargetAccount(transfer);

        
        loadTargetAccount(transfer);

        // Validación general:
        // En una transferencia interna no tiene sentido transferir a la misma cuenta.
        validateDifferentAccounts(transfer);

        // RN-21 / RN-23 / RN-30 / RN-31 / RN-AD04 / RN-AD08:
        // Se valida que el usuario tenga permisos para crear la transferencia
        // y que la cuenta origen le pertenezca o pertenezca a su empresa.
        validateTransferAccess(user, transfer);

        // Datos de creación de la transferencia.
        transfer.setCreatedBy(user);
        transfer.setCreationDate(LocalDateTime.now());

        // RN-AD13:
        // Si es una transferencia empresarial y supera el umbral,
        // queda en espera de aprobación.
        if (requiresApproval(user, transfer.getAmount())) {
            transfer.setStatus(TransferStatus.PENDING_APPROVAL);
            transfer.setExpirationDate(LocalDateTime.now().plusHours(1));

            // Se guarda primero para que exista persistencia real
            // antes de cualquier revisión posterior.
            transferPort.save(transfer);

            // RN-20:
            // Registrar la creación de la transferencia en la bitácora.
            registerCreatedTransferLog(user, transfer);
            return;
        }

        // RN-18 / RN-19 / RN-20:
        // Si no requiere aprobación, se registra primero la transferencia,
        // se deja en estado APPROVED y luego se ejecuta.
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setExpirationDate(null);
        transferPort.save(transfer);

        // RN-20:
        // Registrar la creación de la transferencia en la bitácora.
        registerCreatedTransferLog(user, transfer);

        // Si no requiere aprobación, se ejecuta directamente.
        executeTransfer.executeTransfer(transfer, user);
    }

    private void validateActiveUser(User user) {

        // Validación general:
        // El usuario actor no puede estar inactivo ni bloqueado.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El usuario debe estar activo para crear transferencias");
        }
    }

    private void validateAmount(BigDecimal amount) {

        // RN-14:
        // El monto debe ser mayor que cero.
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto de la transferencia debe ser mayor que cero");
        }
    }

    private void validateTransferType(Transfer transfer) {

        // Validación general:
        // El tipo de transferencia es obligatorio para saber
        // si se acredita una cuenta interna o si la salida es externa.
        if (transfer.getTransferType() == null) {
            throw new BusinessException("El tipo de transferencia es obligatorio");
        }
    }

    private void validateSourceAccount(Transfer transfer) {

        // Validación general:
        // La cuenta origen es obligatoria.
        if (transfer.getSourceAccount() == null) {
            throw new BusinessException("La cuenta origen es obligatoria");
        }
    }

    private void loadSourceAccount(Transfer transfer) {
        if (transfer.getSourceAccount().getAccountNumber() == null ||
            transfer.getSourceAccount().getAccountNumber().trim().isEmpty()) {
            throw new BusinessException("El número de cuenta origen es obligatorio");
        }

        BankAccount sourceAccount = accountPort.findByAccountNumber(
                transfer.getSourceAccount().getAccountNumber().trim()
        );

        if (sourceAccount == null) {
            throw new BusinessException("No existe la cuenta origen");
        }

        transfer.setSourceAccount(sourceAccount);
    }

    private void validateTargetAccount(Transfer transfer) {

        // Validación general:
        // Si la transferencia es interna, la cuenta destino es obligatoria.
        if (transfer.getTransferType() == TransferType.INTERNAL && transfer.getTargetAccount() == null) {
            throw new BusinessException("La cuenta destino es obligatoria para una transferencia interna");
        }
    }

    private void loadTargetAccount(Transfer transfer) {
        if (transfer.getTransferType() != TransferType.INTERNAL) {
            return;
        }

        if (transfer.getTargetAccount() == null ||
            transfer.getTargetAccount().getAccountNumber() == null ||
            transfer.getTargetAccount().getAccountNumber().trim().isEmpty()) {
            throw new BusinessException("El número de cuenta destino es obligatorio para una transferencia interna");
        }

        BankAccount targetAccount = accountPort.findByAccountNumber(
                transfer.getTargetAccount().getAccountNumber().trim()
        );

        if (targetAccount == null) {
            throw new BusinessException("No existe la cuenta destino");
        }

        transfer.setTargetAccount(targetAccount);
    }

    private void validateDifferentAccounts(Transfer transfer) {

        // Validación general:
        // En una transferencia interna la cuenta origen y destino no pueden ser la misma.
        if (transfer.getTransferType() == TransferType.INTERNAL &&
            transfer.getSourceAccount().getAccountNumber() != null &&
            transfer.getTargetAccount() != null &&
            transfer.getTargetAccount().getAccountNumber() != null &&
            transfer.getSourceAccount().getAccountNumber().equals(transfer.getTargetAccount().getAccountNumber())) {
            throw new BusinessException("La cuenta origen y la cuenta destino no pueden ser la misma");
        }
    }

    private void validateTransferAccess(User user, Transfer transfer) {

        // RN-21 / RN-23 / RN-AD04:
        // Cliente persona natural solo puede operar sus propios productos.
        if (user.getSystemRole() == RoleType.INDIVIDUAL_CUSTOMER) {
            if (user.getCustomer() == null || transfer.getSourceAccount().getOwner() == null ||
                !user.getCustomer().getIdentificationNumber()
                    .equals(transfer.getSourceAccount().getOwner().getIdentificationNumber())) {
                throw new BusinessException("El cliente solo puede operar sus propios productos");
            }
            return;
        }

        // RN-30 / RN-31 / RN-AD08:
        // Empleado de empresa solo puede operar productos de su empresa
        // y puede crear transferencias.
        if (user.getSystemRole() == RoleType.COMPANY_OPERATOR) {
            if (user.getCustomer() == null || transfer.getSourceAccount().getOwner() == null ||
                !user.getCustomer().getIdentificationNumber()
                    .equals(transfer.getSourceAccount().getOwner().getIdentificationNumber())) {
                throw new BusinessException("El empleado de empresa solo puede operar productos de su empresa");
            }
            return;
        }

        throw new BusinessException("El usuario no tiene permisos para crear transferencias");
    }

    private boolean requiresApproval(User user, BigDecimal amount) {

        // RN-AD13:
        // Solo aplica flujo de aprobación a transferencias empresariales
        // que superen el umbral definido.
        return user.getSystemRole() == RoleType.COMPANY_OPERATOR &&
               amount.compareTo(APPROVAL_THRESHOLD) > 0;
    }

    private void registerCreatedTransferLog(User user, Transfer transfer) {

        // RN-20:
        // Registrar la creación de la transferencia en la bitácora.
        // El affectedProductId se asocia a la cuenta origen para que
        // FindCustomerHistory pueda consultar el historial por producto.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_CREATED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(transfer.getSourceAccount().getAccountNumber());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("transferId", transfer.getTransferId());
        detailData.put("sourceAccount", transfer.getSourceAccount().getAccountNumber());
        detailData.put("targetAccount", getTargetAccountNumber(transfer));
        detailData.put("transferType", transfer.getTransferType().name());
        detailData.put("amount", transfer.getAmount());
        detailData.put("status", transfer.getStatus().name());
        detailData.put("creationDate", transfer.getCreationDate());

        operationLog.setDetailData(detailData);
        operationLogPort.save(operationLog);
    }

    private String getTargetAccountNumber(Transfer transfer) {
        return transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null;
    }
}