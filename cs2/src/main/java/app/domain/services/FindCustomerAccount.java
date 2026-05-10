package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

@Service
public class FindCustomerAccount {

    private final AccountPort accountPort;
    private final UserPort userPort;

    public FindCustomerAccount(AccountPort accountPort, UserPort userPort) {
        this.accountPort = accountPort;
        this.userPort = userPort;
    }

    public BankAccount findAccount(String userIdentification, String accountNumber) throws BusinessException {

        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BusinessException("El número de cuenta es obligatorio");
        }

        User user = userPort.findByIdentificationNumber(userIdentification.trim());

        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);
        validateCustomerRole(user);

        BankAccount account = accountPort.findByAccountNumber(accountNumber.trim());

        if (account == null) {
            throw new BusinessException("No existe una cuenta con ese número");
        }

        validateAccountOwnership(user, account);

        return account;
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para consultar cuentas");
        }
    }

    private void validateCustomerRole(User user) {
        if (user.getSystemRole() != RoleType.INDIVIDUAL_CUSTOMER &&
                user.getSystemRole() != RoleType.BUSINESS_CUSTOMER) {
            throw new BusinessException("Solo un cliente puede consultar sus propias cuentas");
        }
    }

    private void validateAccountOwnership(User user, BankAccount account) {

        if (account.getOwner() == null || account.getOwner().getIdentificationNumber() == null) {
            throw new BusinessException("La cuenta no tiene titular asociado");
        }

        if (user.getCustomer() == null || user.getCustomer().getIdentificationNumber() == null) {
            throw new BusinessException("El usuario no tiene cliente asociado");
        }

        String customerIdentification = user.getCustomer().getIdentificationNumber();
        String accountOwnerIdentification = account.getOwner().getIdentificationNumber();

        if (!customerIdentification.equals(accountOwnerIdentification)) {
            throw new BusinessException("El cliente no puede consultar cuentas que no le pertenecen");
        }
    }
}