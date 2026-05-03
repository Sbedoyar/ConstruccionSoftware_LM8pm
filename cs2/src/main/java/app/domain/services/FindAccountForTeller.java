package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FindAccountForTeller {

    private final AccountPort accountPort;
    private final UserPort userPort;

    @Autowired
    public FindAccountForTeller(AccountPort accountPort, UserPort userPort) {
        this.accountPort = accountPort;
        this.userPort = userPort;
    }

    public BankAccount findAccount(String userIdentification, String accountNumber) throws BusinessException {

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Validación general:
        // El número de cuenta es obligatorio.
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BusinessException("El número de cuenta es obligatorio");
        }

        // Se busca el usuario que realiza la consulta.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Validación adicional:
        // El usuario actor debe estar activo.
        validateActiveUser(user);

        // RN-24 / RN-26 / RN-AD06:
        // Solo el empleado de ventanilla puede consultar saldo y estado de cuentas
        // para propósitos de caja.
        validateTellerRole(user);

        // RN-24:
        // El empleado de ventanilla puede consultar cualquier cuenta
        // para conocer saldo y estado de cuenta.
        BankAccount account = accountPort.findByAccountNumber(accountNumber.trim());
        if (account == null) {
            throw new BusinessException("No existe una cuenta con ese número");
        }

        return account;
    }

    private void validateActiveUser(User user) {

        // Validación adicional:
        // El usuario actor no puede estar inactivo o bloqueado.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El usuario debe estar activo para consultar cuentas");
        }
    }

    private void validateTellerRole(User user) {

        // RN-24 / RN-26 / RN-AD06:
        // Solo el rol TELLER_EMPLOYEE puede usar esta consulta de caja.
        if (user.getSystemRole() != RoleType.TELLER_EMPLOYEE) {
            throw new BusinessException("Solo un empleado de ventanilla puede consultar cuentas para operaciones de caja");
        }
    }
}
