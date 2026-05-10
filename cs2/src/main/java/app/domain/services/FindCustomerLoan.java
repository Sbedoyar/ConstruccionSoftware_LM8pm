package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.LoanPort;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

@Service
public class FindCustomerLoan {

    private final LoanPort loanPort;
    private final UserPort userPort;

    public FindCustomerLoan(LoanPort loanPort, UserPort userPort) {
        this.loanPort = loanPort;
        this.userPort = userPort;
    }

    public Loan findLoan(String userIdentification, String loanId) throws BusinessException {

        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        User user = userPort.findByIdentificationNumber(userIdentification.trim());

        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);
        validateCustomerRole(user);

        Loan loan = loanPort.findByLoanId(loanId.trim());

        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        validateLoanOwnership(user, loan);

        return loan;
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para consultar préstamos");
        }
    }

    private void validateCustomerRole(User user) {
        if (user.getSystemRole() != RoleType.INDIVIDUAL_CUSTOMER &&
                user.getSystemRole() != RoleType.BUSINESS_CUSTOMER) {
            throw new BusinessException("Solo un cliente puede consultar sus propios préstamos");
        }
    }

    private void validateLoanOwnership(User user, Loan loan) {

        if (user.getCustomer() == null ||
                user.getCustomer().getIdentificationNumber() == null) {
            throw new BusinessException("El usuario no tiene cliente asociado");
        }

        if (loan.getOwner() == null ||
                loan.getOwner().getIdentificationNumber() == null) {
            throw new BusinessException("El préstamo no tiene cliente asociado");
        }

        String userCustomerIdentification = user.getCustomer().getIdentificationNumber();
        String loanOwnerIdentification = loan.getOwner().getIdentificationNumber();

        if (!userCustomerIdentification.equals(loanOwnerIdentification)) {
            throw new BusinessException("El cliente no puede consultar préstamos que no le pertenecen");
        }
    }
}