package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.LoanPort;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindCustomerHistory {

    private final OperationLogPort operationLogPort;
    private final UserPort userPort;
    private final AccountPort accountPort;
    private final LoanPort loanPort;

    @Autowired
    public FindCustomerHistory(OperationLogPort operationLogPort,
                               UserPort userPort,
                               AccountPort accountPort,
                               LoanPort loanPort) {
        this.operationLogPort = operationLogPort;
        this.userPort = userPort;
        this.accountPort = accountPort;
        this.loanPort = loanPort;
    }

    public List<OperationLog> findHistoryByProduct(String userIdentification,
                                                   String affectedProductId) throws BusinessException {

        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        if (affectedProductId == null || affectedProductId.trim().isEmpty()) {
            throw new BusinessException("El ID del producto afectado es obligatorio");
        }

        String normalizedProductId = affectedProductId.trim();

        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);
        validateCustomerRole(user);
        validateCustomerOwnership(user, normalizedProductId);

        return operationLogPort.findByAffectedProductId(normalizedProductId);
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para realizar esta operación");
        }
    }

    private void validateCustomerRole(User user) {
        if (user.getSystemRole() != RoleType.INDIVIDUAL_CUSTOMER &&
            user.getSystemRole() != RoleType.BUSINESS_CUSTOMER) {
            throw new BusinessException("Solo un cliente puede consultar su historial de operaciones");
        }
    }

    private void validateCustomerOwnership(User user, String affectedProductId) {
        if (user.getCustomer() == null ||
                user.getCustomer().getIdentificationNumber() == null ||
                user.getCustomer().getIdentificationNumber().trim().isEmpty()) {
            throw new BusinessException("El usuario no tiene un cliente asociado");
        }

        String customerIdentification = user.getCustomer().getIdentificationNumber();

        BankAccount account = accountPort.findByAccountNumber(affectedProductId);
        if (account != null) {
            validateAccountOwnership(account, customerIdentification);
            return;
        }

        Loan loan = loanPort.findByLoanId(affectedProductId);
        if (loan != null) {
            validateLoanOwnership(loan, customerIdentification);
            return;
        }

        throw new BusinessException("No existe un producto asociado a ese identificador");
    }

    private void validateAccountOwnership(BankAccount account, String customerIdentification) {
        if (account.getOwner() == null ||
                account.getOwner().getIdentificationNumber() == null ||
                !customerIdentification.equals(account.getOwner().getIdentificationNumber())) {
            throw new BusinessException("El cliente no puede consultar historial de productos que no le pertenecen");
        }
    }

    private void validateLoanOwnership(Loan loan, String customerIdentification) {
        if (loan.getOwner() == null ||
                loan.getOwner().getIdentificationNumber() == null ||
                !customerIdentification.equals(loan.getOwner().getIdentificationNumber())) {
            throw new BusinessException("El cliente no puede consultar historial de productos que no le pertenecen");
        }
    }
}