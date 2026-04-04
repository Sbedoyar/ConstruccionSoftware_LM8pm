package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.ProductCategory;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.AccountPort;
import app.domain.ports.CustomerPort;
import app.domain.ports.OperationLogPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CreateAccount {

    private final AccountPort accountPort;
    private final CustomerPort customerPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    @Autowired
    public CreateAccount(AccountPort accountPort,
                         CustomerPort customerPort,
                         UserPort userPort,
                         OperationLogPort operationLogPort) {
        this.accountPort = accountPort;
        this.customerPort = customerPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    public void createAccount(String customerIdentification, String userIdentification, BankAccount account) throws BusinessException {

        // Validación general de entrada.
        // No corresponde a una RN numerada, pero evita procesar una cuenta nula.
        if (account == null) {
            throw new BusinessException("La cuenta no puede ser null");
        }

        // Validación general:
        // La identificación del cliente objetivo es obligatoria.
        if (customerIdentification == null || customerIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del cliente es obligatoria");
        }

        // Validación general:
        // La identificación del usuario actor es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca el cliente asociado a la apertura de la cuenta.
        Customer customer = customerPort.findByIdentificationNumber(customerIdentification.trim());
        if (customer == null) {
            throw new BusinessException("No existe un cliente con esa identificación");
        }

        // Se busca el usuario que está intentando realizar la apertura.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // RN-02:
        // El flujo de apertura no debe ser ejecutado por un usuario inactivo o bloqueado.
        validateActorUserStatus(user);

        // RN-02:
        // No se puede abrir una cuenta a un cliente cuyo estado esté inactivo o bloqueado.
        // En tu modelo el estado operativo del cliente está representado por CustomerStatus.
        validateTargetCustomerStatus(customer);

        // RN-24 / RN-AD07:
        // La apertura de nuevas cuentas puede ser registrada por:
        // - Empleado de ventanilla
        // - Empleado comercial
        validateAuthorizedRole(user);

        // RN-03:
        // Cada cuenta bancaria debe tener un número de cuenta único.
        validateAccountNumber(account.getAccountNumber());
        validateUniqueAccountNumber(account.getAccountNumber().trim());

        // RN-04:
        // El tipo de cuenta debe ser un valor válido del Producto Bancario General (catálogo).
        validateProductCatalog(account.getCatalog());

        // Validación general:
        // El saldo inicial no puede ser negativo.
        validateInitialBalance(account);

        // Se normaliza el número de cuenta antes de guardar.
        account.setAccountNumber(account.getAccountNumber().trim());

        // Se asigna el cliente como titular de la cuenta.
        account.setOwner(customer);

        // Regla general de apertura:
        // Si no se envió fecha de apertura, se asigna la fecha actual.
        if (account.getOpeningDate() == null) {
            account.setOpeningDate(LocalDate.now());
        }

        // Regla general de apertura:
        // Si no se definió estado, la cuenta inicia como activa.
        if (account.getAccountStatus() == null) {
            account.setAccountStatus(AccountStatus.ACTIVE);
        }

        // Si todas las reglas se cumplen, se guarda la cuenta.
        accountPort.save(account);

        // RN-20:
        // Registrar la apertura de la cuenta en la bitácora.
        registerAccountCreatedLog(user, account);
    }

    private void validateActorUserStatus(User user) {

        // RN-02:
        // El usuario actor no puede estar inactivo ni bloqueado al momento
        // de abrir una cuenta para un cliente.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("No se puede abrir una cuenta con un usuario inactivo o bloqueado");
        }
    }

    private void validateTargetCustomerStatus(Customer customer) {

        // RN-02:
        // El cliente objetivo debe estar activo para recibir una nueva cuenta.
        if (customer.getCustomerStatus() == CustomerStatus.INACTIVE ||
            customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new BusinessException("No se puede abrir una cuenta a un cliente inactivo o bloqueado");
        }
    }

    private void validateAuthorizedRole(User user) {

        // RN-24:
        // Empleado de ventanilla puede registrar apertura de nuevas cuentas.
        //
        // RN-AD07:
        // Empleado comercial puede crear solicitudes de nuevos productos
        // en nombre del cliente.
        if (user.getSystemRole() != RoleType.TELLER_EMPLOYEE &&
            user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessException("El usuario no tiene permisos para crear cuentas");
        }
    }

    private void validateAccountNumber(String accountNumber) {

        // RN-03:
        // El número de cuenta es obligatorio.
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BusinessException("El número de cuenta es obligatorio");
        }
    }

    private void validateUniqueAccountNumber(String accountNumber) {

        // RN-03:
        // No puede existir otra cuenta con el mismo número.
        BankAccount existingAccount = accountPort.findByAccountNumber(accountNumber);

        if (existingAccount != null) {
            throw new BusinessException("Ya existe una cuenta con ese número");
        }
    }

    private void validateProductCatalog(BankProductCatalog catalog) {

        // RN-04:
        // La cuenta debe estar asociada a un producto válido del catálogo.
        if (catalog == null) {
            throw new BusinessException("La cuenta debe estar asociada a un producto del catálogo");
        }

        // RN-04:
        // El código del producto es obligatorio.
        if (catalog.getProductCode() == null || catalog.getProductCode().trim().isEmpty()) {
            throw new BusinessException("El código del producto es obligatorio");
        }

        // RN-04:
        // El nombre del producto es obligatorio.
        if (catalog.getProductName() == null || catalog.getProductName().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }

        // RN-04:
        // La categoría debe corresponder a productos de cuentas.
        if (catalog.getCategory() != ProductCategory.ACCOUNT) {
            throw new BusinessException("El catálogo asociado no corresponde a un producto de tipo cuenta");
        }

        // RN-04:
        // El producto del catálogo debe estar activo para poder ser usado.
        if (!catalog.isActive()) {
            throw new BusinessException("El producto del catálogo no se encuentra activo");
        }
    }

    private void validateInitialBalance(BankAccount account) {

        // Validación general:
        // El saldo inicial no puede ser negativo.
        if (account.getBalance() != null && account.getBalance().signum() < 0) {
            throw new BusinessException("El saldo inicial de la cuenta no puede ser negativo");
        }
    }

    private void registerAccountCreatedLog(User user, BankAccount account) {

        // RN-20:
        // Registrar la apertura de la cuenta en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.ACCOUNT_CREATED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(account.getAccountNumber());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("accountNumber", account.getAccountNumber());
        detailData.put("accountType", account.getAccountType() != null ? account.getAccountType().name() : null);
        detailData.put("currency", account.getCurrency() != null ? account.getCurrency().name() : null);
        detailData.put("accountStatus", account.getAccountStatus() != null ? account.getAccountStatus().name() : null);
        detailData.put("openingDate", account.getOpeningDate());
        detailData.put("ownerIdentification", account.getOwner() != null ? account.getOwner().getIdentificationNumber() : null);

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}
