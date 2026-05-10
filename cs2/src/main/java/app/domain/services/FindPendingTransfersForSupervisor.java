package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindPendingTransfersForSupervisor {

    private final TransferPort transferPort;
    private final UserPort userPort;
    private final AccountPort accountPort;

    public FindPendingTransfersForSupervisor(TransferPort transferPort,
                                             UserPort userPort,
                                             AccountPort accountPort) {
        this.transferPort = transferPort;
        this.userPort = userPort;
        this.accountPort = accountPort;
    }

    public List<Transfer> findPendingTransfers(String supervisorIdentification) throws BusinessException {

        if (supervisorIdentification == null || supervisorIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del supervisor es obligatoria");
        }

        User supervisor = userPort.findByIdentificationNumber(supervisorIdentification.trim());

        if (supervisor == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(supervisor);
        validateSupervisorRole(supervisor);
        validateCompanyAssociation(supervisor);

        String companyIdentification = supervisor.getCustomer().getIdentificationNumber();

        List<Transfer> pendingTransfers = transferPort.findByStatus(TransferStatus.PENDING_APPROVAL);

        return pendingTransfers.stream()
                .filter(transfer -> belongsToSupervisorCompany(transfer, companyIdentification))
                .toList();
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para consultar transferencias pendientes");
        }
    }

    private void validateSupervisorRole(User user) {
        if (user.getSystemRole() != RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("Solo un supervisor de empresa puede consultar transferencias pendientes");
        }
    }

    private void validateCompanyAssociation(User user) {
        if (user.getCustomer() == null ||
                user.getCustomer().getIdentificationNumber() == null ||
                user.getCustomer().getIdentificationNumber().trim().isEmpty()) {
            throw new BusinessException("El supervisor no tiene empresa asociada");
        }
    }

    private boolean belongsToSupervisorCompany(Transfer transfer, String companyIdentification) {

        if (transfer == null ||
                transfer.getSourceAccount() == null ||
                transfer.getSourceAccount().getAccountNumber() == null) {
            return false;
        }

        BankAccount sourceAccount = accountPort.findByAccountNumber(
                transfer.getSourceAccount().getAccountNumber()
        );

        if (sourceAccount == null ||
                sourceAccount.getOwner() == null ||
                sourceAccount.getOwner().getIdentificationNumber() == null) {
            return false;
        }

        return companyIdentification.equals(sourceAccount.getOwner().getIdentificationNumber());
    }
}