package app.domain.services;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.transfer.Transfer;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.AccountPort;
import app.domain.ports.TransferPort;
import app.domain.ports.OperationLogPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferService {

    private final AccountPort accountPort;
    private final TransferPort transferPort;
    private final OperationLogPort logPort;
    private final EmployeeAccessService employeeAccessService;

    public TransferService(AccountPort accountPort, TransferPort transferPort, OperationLogPort logPort, EmployeeAccessService employeeAccessService) {
        this.accountPort = accountPort;
        this.transferPort = transferPort;
        this.logPort = logPort;
        this.employeeAccessService = employeeAccessService;
    }

    public void executeTransfer(Transfer transfer, User user) {

        employeeAccessService.validateNoBalanceOperations(user);

        // ==============================
        // Validación defensiva
        // ==============================
        if (transfer == null) {
            throw new BusinessExceptions("Transferencia inválida");
        }

        // ==============================
        // RN14: ID único + monto > 0
        // Toda transferencia debe tener un ID_Transferencia único 
        // y el Monto a transferir debe ser estrictamente mayor que cero.
        // ==============================
        Transfer existing = transferPort.findById(transfer.getTransferId());

        if (existing != null) {
            throw new BusinessExceptions("La transferencia ya existe");
        }

        if (transfer.getAmount() == null ||
            transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessExceptions("El monto debe ser mayor que cero");
        }

        // ==============================
        // Obtener cuentas
        // ==============================
        BankAccount origin = transfer.getSourceAccount();
        BankAccount destination = transfer.getTargetAccount();

        // 1. validar objetos
        if (origin == null || destination == null) {
            throw new BusinessExceptions("Cuenta inválida");
        }

        // 2. validar datos internos (opcional)
        if (origin.getAccountNumber() == null ||
            destination.getAccountNumber() == null) {

            throw new BusinessExceptions("Número de cuenta inválido");
        }
        // 3. Verifica que ambas cuentas tengan saldo (no sean null)
        if (origin.getBalance() == null || destination.getBalance() == null) {
            throw new BusinessExceptions("Saldo inválido");
        }

        // ==============================
        // Validar misma cuenta
        // ==============================
        if (origin.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new BusinessExceptions("No puede transferir a la misma cuenta");
        }

        // ==============================
        // RN16: Cuenta origen operativa
        // No se permiten transferencias desde Cuenta_Origen bloqueadas o canceladas.
        // ==============================
        if (origin.getAccountStatus() == AccountStatus.BLOCKED ||
            origin.getAccountStatus() == AccountStatus.CANCELLED) {

            throw new BusinessExceptions("Cuenta origen no operativa");
        }

        // ==============================
        // RN15: Saldo suficiente
        // No se permite ejecutar transferencias (pasar a estado "Ejecutada") 
        // desde Cuenta_Origen con Saldo_Actual insuficiente, 
        // a menos que exista una regla específica de sobregiro autorizado 
        // que deba ser validada.
        // ==============================
            if (origin.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new BusinessExceptions("Saldo insuficiente");
        }

        // ==============================
        // Guardar valores previos (bitácora), serviran para la bitacora
        // ==============================
        BigDecimal previousOriginBalance = origin.getBalance();
        BigDecimal previousDestinationBalance = destination.getBalance();

        // ==============================
        // RN18: Disminuir saldo origen
        // Disminuir el Saldo_Actual de la Cuenta_Origen.
        // ==============================
        origin.setBalance(
                origin.getBalance().subtract(transfer.getAmount())
        );
        

        // ==============================
        // RN19: Aumentar saldo destino
        // Aumentar el Saldo_Actual de la Cuenta_Destino (si es interna).
        // ==============================
        if (destination.isInternal()) {
            destination.setBalance(
                destination.getBalance().add(transfer.getAmount())
            );
        }

        // ==============================
        // Cambio de estado
        // ==============================
        transfer.setStatus(TransferStatus.EXECUTED);

        // ==============================
        // RN20: Guardar en BD
        // Registrar la operación en la Base de Datos Relacional 
        // y en la Bitácora NoSQL.
        // ==============================
        accountPort.save(origin);
        accountPort.save(destination);
        transferPort.save(transfer);

        // ==============================
        // RN20: Registrar en bitácora
        // ==============================
        OperationLog log = new OperationLog();

        log.setOperationType(OperationType.TRANSFER_EXECUTED);
        log.setTimestamp(LocalDateTime.now());
        log.setAffectedProductId(origin.getProductCode());

        Map<String, Object> details = new HashMap<>();  

        details.put("amount", transfer.getAmount());
        details.put("originAccount", origin.getAccountNumber());
        details.put("destinationAccount", destination.getAccountNumber());

        details.put("originBalanceBefore", previousOriginBalance);
        details.put("originBalanceAfter", origin.getBalance());

        details.put("destinationBalanceBefore", previousDestinationBalance);
        details.put("destinationBalanceAfter", destination.getBalance());

        log.setDetailData(details);

        logPort.save(log);
    }

    // ==========================================
    // RN17: Vencimiento de transferencias
    // Como se detalla en los flujos de aprobación, 
    // si una transferencia que requiere aprobación (empresa de alto monto)
    // permanece en estado "En espera de aprobación" por más de una hora, 
    // debe cambiar automáticamente a "Vencida" y registrar el evento en la Bitácora.
    // ==========================================
    public void expirePendingTransfers() {

        List<Transfer> pendientes = transferPort.findPendingTransfers();

        for (Transfer t : pendientes) {

            if (t.getCreationDate().plusHours(1)
                    .isBefore(LocalDateTime.now())) {

                t.setStatus(TransferStatus.EXPIRED);
                transferPort.save(t);

                OperationLog log = new OperationLog();
                log.setOperationType(OperationType.TRANSFER_EXPIRED);
                log.setTimestamp(LocalDateTime.now());
                log.setAffectedProductId(String.valueOf(t.getTransferId()));

                logPort.save(log);
            }
        }
    }
}