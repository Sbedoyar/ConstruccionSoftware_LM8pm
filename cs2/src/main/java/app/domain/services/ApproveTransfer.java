package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.TransferPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ApproveTransfer {

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final ExecuteTransfer executeTransfer;

    @Autowired
    public ApproveTransfer(TransferPort transferPort, UserPort userPort, ExecuteTransfer executeTransfer) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.executeTransfer = executeTransfer;
    }

    public void approveTransfer(int transferId, String userIdentification) throws BusinessException {

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

        // Se busca el usuario aprobador.
        User user = userPort.findByIdentificationNumber(userIdentification);
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

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

        // Se registra quién aprobó y cuándo lo hizo.
        transfer.setReviewedBy(user);
        transfer.setReviewDate(LocalDate.now());

        // RN-AD14:
        // Si se aprueba, la transferencia debe ejecutarse.
        executeTransfer.executeTransfer(transfer, user);
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
        if (user.getCustomer() == null || sourceAccount.getOwner() == null ||
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
}