package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.AccountPort;
import app.domain.ports.OperationLogPort;
import app.domain.ports.TransferPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExecuteTransfer {

    private final AccountPort accountPort;
    private final TransferPort transferPort;
    private final OperationLogPort operationLogPort;

    @Autowired
    public ExecuteTransfer(AccountPort accountPort, TransferPort transferPort, OperationLogPort operationLogPort) {
        this.accountPort = accountPort;
        this.transferPort = transferPort;
        this.operationLogPort = operationLogPort;
    }

    @Transactional
    public void executeTransfer(Transfer transfer, User user) throws BusinessException {

        // Validación general de entrada.
        if (transfer == null) {
            throw new BusinessException("La transferencia no puede ser null");
        }

        if (user == null) {
            throw new BusinessException("El usuario no puede ser null");
        }

        // Validación general:
        // El monto debe ser mayor que cero.
        validateAmount(transfer.getAmount());

        // Validación general:
        // El tipo de transferencia es obligatorio para definir
        // si la cuenta destino se acredita dentro del banco o no.
        validateTransferType(transfer);

        // Validación adicional:
        // La transferencia debe estar en un estado válido para ejecutarse.
        validateTransferStatusForExecution(transfer);

        // RN-16:
        // No se permiten transferencias desde cuentas bloqueadas o canceladas.
        validateSourceAccountStatus(transfer.getSourceAccount());

        // RN-15:
        // La cuenta origen debe tener fondos suficientes.
        validateSufficientFunds(transfer.getSourceAccount(), transfer.getAmount());

        BigDecimal sourceBalanceBefore = transfer.getSourceAccount().getBalance();
        BigDecimal targetBalanceBefore = null;

        // RN-18:
        // Disminuir el saldo de la cuenta origen.
        transfer.getSourceAccount().setBalance(
            transfer.getSourceAccount().getBalance().subtract(transfer.getAmount())
        );

        // RN-19:
        // Solo si la transferencia es interna se acredita la cuenta destino.
        if (transfer.getTransferType() == TransferType.INTERNAL) {
            validateTargetAccountStatus(transfer.getTargetAccount());
            targetBalanceBefore = transfer.getTargetAccount().getBalance();

            transfer.getTargetAccount().setBalance(
                transfer.getTargetAccount().getBalance().add(transfer.getAmount())
            );
        }

        // Se actualizan las cuentas afectadas.
        accountPort.update(transfer.getSourceAccount());

        if (transfer.getTransferType() == TransferType.INTERNAL) {
            accountPort.update(transfer.getTargetAccount());
        }

        // RN-20 / RN-AD14:
        // La transferencia queda ejecutada.
        transfer.setStatus(TransferStatus.EXECUTED);
        transferPort.update(transfer);

        // RN-20:
        // Registrar la operación en la bitácora.
        registerExecutedTransferLog(user, transfer, sourceBalanceBefore, targetBalanceBefore);
    }

    private void validateAmount(BigDecimal amount) {

        // Validación general:
        // El monto debe ser mayor que cero.
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto de la transferencia debe ser mayor que cero");
        }
    }

    private void validateTransferType(Transfer transfer) {

        // Validación general:
        // El tipo de transferencia es obligatorio.
        if (transfer.getTransferType() == null) {
            throw new BusinessException("El tipo de transferencia es obligatorio");
        }
    }

    private void validateTransferStatusForExecution(Transfer transfer) {

        // Validación adicional:
        // Solo se puede ejecutar una transferencia que esté aprobada
        // o pendiente de ejecución directa.
        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new BusinessException("La transferencia no está en un estado válido para ejecutarse");
        }
    }

    private void validateSourceAccountStatus(BankAccount sourceAccount) {

        // RN-16:
        // La cuenta origen debe estar activa.
        if (sourceAccount == null || sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("No se permiten transferencias desde cuentas bloqueadas o canceladas");
        }
    }

    private void validateTargetAccountStatus(BankAccount targetAccount) {

        // Validación adicional:
        // Si la transferencia es interna, la cuenta destino debe existir y estar activa.
        if (targetAccount == null || targetAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("La cuenta destino debe estar activa para una transferencia interna");
        }
    }

    private void validateSufficientFunds(BankAccount sourceAccount, BigDecimal amount) {

        // RN-15:
        // La cuenta origen debe tener saldo suficiente.
        if (sourceAccount.getBalance() == null || sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("La cuenta origen no tiene fondos suficientes");
        }
    }

    private void registerExecutedTransferLog(User user,
                                             Transfer transfer,
                                             BigDecimal sourceBalanceBefore,
                                             BigDecimal targetBalanceBefore) {

        // RN-20:
        // Registrar la ejecución de la transferencia en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_EXECUTED);
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
        detailData.put("sourceBalanceBefore", sourceBalanceBefore);
        detailData.put("sourceBalanceAfter", transfer.getSourceAccount().getBalance());
        detailData.put("targetBalanceBefore", targetBalanceBefore);
        detailData.put("targetBalanceAfter",
            transfer.getTransferType() == TransferType.INTERNAL && transfer.getTargetAccount() != null
                ? transfer.getTargetAccount().getBalance()
                : null);

        operationLog.setDetailData(detailData);
        operationLogPort.save(operationLog);
    }

    private String getTargetAccountNumber(Transfer transfer) {
        return transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null;
    }
}
