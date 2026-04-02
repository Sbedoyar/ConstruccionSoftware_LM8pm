package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.AccountPort;
import app.domain.ports.OperationLogPort;
import app.domain.ports.TransferPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void executeTransfer(Transfer transfer, User user) throws BusinessException {

        // Validación general de entrada.
        if (transfer == null) {
            throw new BusinessException("La transferencia no puede ser null");
        }

        if (user == null) {
            throw new BusinessException("El usuario no puede ser null");
        }

        // RN-16:
        // No se permiten transferencias desde cuentas bloqueadas o canceladas.
        validateSourceAccountStatus(transfer.getSourceAccount());

        // Validación adicional:
        // La cuenta destino debe estar activa para recibir la transferencia.
        validateTargetAccountStatus(transfer.getTargetAccount());

        // RN-15:
        // La cuenta origen debe tener fondos suficientes.
        validateSufficientFunds(transfer.getSourceAccount(), transfer.getAmount());

        // RN-18:
        // Disminuir el saldo de la cuenta origen.
        transfer.getSourceAccount().setBalance(
            transfer.getSourceAccount().getBalance().subtract(transfer.getAmount())
        );

        // RN-19:
        // Aumentar el saldo de la cuenta destino.
        // En esta versión del proyecto, todas las transferencias se manejan como internas.
        transfer.getTargetAccount().setBalance(
            transfer.getTargetAccount().getBalance().add(transfer.getAmount())
        );

        // Se actualizan las cuentas afectadas.
        accountPort.update(transfer.getSourceAccount());
        accountPort.update(transfer.getTargetAccount());

        // RN-20 / RN-AD14:
        // La transferencia queda ejecutada.
        transfer.setStatus(TransferStatus.EXECUTED);
        transferPort.update(transfer);

        // RN-20:
        // Registrar la operación en la bitácora.
        registerExecutedTransferLog(user, transfer);
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
        // La cuenta destino debe estar activa para recibir la transferencia.
        if (targetAccount == null || targetAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("La cuenta destino debe estar activa");
        }
    }

    private void validateSufficientFunds(BankAccount sourceAccount, java.math.BigDecimal amount) {

        // RN-15:
        // La cuenta origen debe tener saldo suficiente.
        if (sourceAccount.getBalance() == null || sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("La cuenta origen no tiene fondos suficientes");
        }
    }

    private void registerExecutedTransferLog(User user, Transfer transfer) {

        // RN-20:
        // Registrar la ejecución de la transferencia en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.TRANSFER_EXECUTED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(String.valueOf(transfer.getTransferId()));

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("transferId", transfer.getTransferId());
        detailData.put("sourceAccount", transfer.getSourceAccount().getAccountNumber());
        detailData.put("targetAccount", transfer.getTargetAccount().getAccountNumber());
        detailData.put("amount", transfer.getAmount());
        detailData.put("status", transfer.getStatus().name());

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}