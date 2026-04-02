package app.domain.services;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.AccountPort;

public class AccountService {

    private final AccountPort accountPort;
    private final EmployeeAccessService employeeAccessService;

    public AccountService(AccountPort accountPort, EmployeeAccessService employeeAccessService) {
        this.accountPort = accountPort;
        this.employeeAccessService = employeeAccessService;
    }

    public void createAccount (BankAccount account, Customer customer, User user) {

        // ==============================
        // Validación básica
        // ==============================
        if (account == null || customer == null || user == null) {
            throw new BusinessExceptions("Datos inválidos");
        }

//====================================================================================================

        // RN2: No se puede abrir una cuenta a un cliente 
        // cuyo Estado_Usuario esté como 
        // 'Inactivo' o 'Bloqueado'.

        if (user.getUserStatus() == UserStatus.INACTIVE ||
            user.getUserStatus() == UserStatus.BLOCKED) {

            throw new BusinessExceptions("No se puede crear cuenta: el usuario del cliente está inactivo o bloqueado");
        }
        // ==============================
        // (Extra recomendado) Cliente activo
        // ==============================
        if (customer.getCustomerStatus() == CustomerStatus.INACTIVE ||
            customer.getCustomerStatus() == CustomerStatus.BLOCKED) {

            throw new BusinessExceptions("El cliente no está activo");
        }

//====================================================================================================
        
        // ==============================
        // RN25: Solo TELLER puede crear cuentas
        // ==============================
        employeeAccessService.validateAccountCreation(user);

        // RN3: Cada Cuenta Bancaria 
        // debe tener un Numero_Cuenta único.
        
        if (account.getAccountNumber() == null ||
            account.getAccountNumber().trim().isEmpty()) {

            throw new BusinessExceptions("Número de cuenta inválido");
        }

        String accountNumber = account.getAccountNumber().trim();

        BankAccount existing = accountPort.findByAccountNumber(accountNumber);

        if (existing != null) {
            throw new BusinessExceptions("El número de cuenta ya existe");
        }

        // Normalizar
        account.setAccountNumber(accountNumber);

//====================================================================================================

        // RN4: El Tipo_Cuenta 
        // debe ser un valor válido del Producto Bancario General (catálogo).

        if (account.getAccountType() == null) {
            throw new BusinessExceptions("Tipo de cuenta inválido");
        }

        //Guardar cuenta
        accountPort.save(account);
    }

//====================================================================================================

    // RN5: No se permiten operaciones (transferencias, retiros) 
    // en cuentas con Estado_Cuenta 'Bloqueada' o 'Cancelada', 
    // salvo procesos internos de cierre.(Preguntar)

    public void validateAccountActive (BankAccount account){

        if (account == null) {
            throw new BusinessExceptions("Cuenta inválida");
        }

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessExceptions("La cuenta no permite operaciones");
        }
    }
}

