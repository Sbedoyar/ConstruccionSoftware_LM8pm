package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.TransferPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CreateTransfer {

    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000000");

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final ExecuteTransfer executeTransfer;

    @Autowired
    public CreateTransfer(TransferPort transferPort, UserPort userPort, ExecuteTransfer executeTransfer) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.executeTransfer = executeTransfer;
    }

    public void createTransfer(String userIdentification, Transfer transfer) throws BusinessException {

        // Validación general de entrada.
        if (transfer == null) {
            throw new BusinessException("La transferencia no puede ser null");
        }

        // Se busca el usuario creador.
        User user = userPort.findByIdentificationNumber(userIdentification);
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // RN-14:
        // Toda transferencia debe tener un ID único y obligatorio.
        validateTransferId(transfer.getTransferId());
        validateUniqueTransferId(transfer.getTransferId());

        // RN-14:
        // El monto debe ser estrictamente mayor que cero.
        validateAmount(transfer.getAmount());

        // Validación general:
        // La cuenta origen es obligatoria.
        validateSourceAccount(transfer);

        // Validación general:
        // La cuenta destino es obligatoria.
        validateTargetAccount(transfer);

        // RN-21 / RN-23 / RN-30 / RN-31 / RN-AD04 / RN-AD08:
        // Se valida que el usuario tenga permisos para crear la transferencia
        // y que la cuenta origen le pertenezca o pertenezca a su empresa.
        validateTransferAccess(user, transfer);

        // Datos de creación de la transferencia.
        transfer.setCreatedBy(user);
        transfer.setCreationDate(LocalDateTime.now());

        // RN-AD13:
        // Si es una transferencia empresarial y supera el umbral,
        // queda en espera de aprobación. Si no, se ejecuta directamente.
        if (requiresApproval(user, transfer.getAmount())) {
            transfer.setStatus(TransferStatus.PENDING_APPROVAL);
            transfer.setExpirationDate(LocalDateTime.now().plusHours(1));
            transferPort.save(transfer);
            return;
        }

        // Si no requiere aprobación, se ejecuta directamente.
        executeTransfer.executeTransfer(transfer, user);
    }

    private void validateTransferId(int transferId) {

        // RN-14:
        // El ID de la transferencia es obligatorio.
        if (transferId <= 0) {
            throw new BusinessException("El ID de la transferencia es obligatorio y debe ser mayor que cero");
        }
    }

    private void validateUniqueTransferId(int transferId) {

        // RN-14:
        // El ID de la transferencia debe ser único.
        if (transferPort.findByTransferId(transferId) != null) {
            throw new BusinessException("Ya existe una transferencia con ese ID");
        }
    }

    private void validateAmount(BigDecimal amount) {

        // RN-14:
        // El monto debe ser mayor que cero.
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto de la transferencia debe ser mayor que cero");
        }
    }

    private void validateSourceAccount(Transfer transfer) {
        if (transfer.getSourceAccount() == null) {
            throw new BusinessException("La cuenta origen es obligatoria");
        }
    }

    private void validateTargetAccount(Transfer transfer) {
        if (transfer.getTargetAccount() == null) {
            throw new BusinessException("La cuenta destino es obligatoria");
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
}