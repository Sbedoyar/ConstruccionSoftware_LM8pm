package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

@Service
public class FindCompanyOperatorTransfer {

    private final TransferPort transferPort;
    private final UserPort userPort;

    public FindCompanyOperatorTransfer(TransferPort transferPort,
                                       UserPort userPort) {
        this.transferPort = transferPort;
        this.userPort = userPort;
    }

    public Transfer findTransfer(String operatorIdentification, int transferId) throws BusinessException {

        if (operatorIdentification == null || operatorIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del operador es obligatoria");
        }

        User operator = userPort.findByIdentificationNumber(operatorIdentification.trim());

        if (operator == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(operator);
        validateCompanyOperatorRole(operator);
        validateCompanyAssociation(operator);

        Transfer transfer = transferPort.findByTransferId(transferId);

        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        validateTransferCreatedByOperator(operator, transfer);

        return transfer;
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para consultar transferencias");
        }
    }

    private void validateCompanyOperatorRole(User user) {
        if (user.getSystemRole() != RoleType.COMPANY_OPERATOR) {
            throw new BusinessException("Solo un operador de empresa puede consultar sus transferencias");
        }
    }

    private void validateCompanyAssociation(User user) {
        if (user.getCustomer() == null ||
                user.getCustomer().getIdentificationNumber() == null ||
                user.getCustomer().getIdentificationNumber().trim().isEmpty()) {
            throw new BusinessException("El operador no tiene empresa asociada");
        }
    }

    private void validateTransferCreatedByOperator(User operator, Transfer transfer) {
        if (transfer.getCreatedBy() == null ||
                transfer.getCreatedBy().getIdentificationNumber() == null) {
            throw new BusinessException("La transferencia no tiene usuario creador asociado");
        }

        if (!operator.getIdentificationNumber().equals(transfer.getCreatedBy().getIdentificationNumber())) {
            throw new BusinessException("El operador no puede consultar transferencias que no creó");
        }
    }
}