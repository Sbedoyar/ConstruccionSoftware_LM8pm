package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import app.domain.ports.out.AccountPort;
import app.domain.ports.out.CustomerPort;
import app.domain.ports.out.LoanPort;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.TransferPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class FindDataForInternalAnalyst {

    private final UserPort userPort;
    private final CustomerPort customerPort;
    private final AccountPort accountPort;
    private final LoanPort loanPort;
    private final TransferPort transferPort;
    private final OperationLogPort operationLogPort;

    //@Autowired
    public FindDataForInternalAnalyst(UserPort userPort,
                                      CustomerPort customerPort,
                                      AccountPort accountPort,
                                      LoanPort loanPort,
                                      TransferPort transferPort,
                                      OperationLogPort operationLogPort) {
        this.userPort = userPort;
        this.customerPort = customerPort;
        this.accountPort = accountPort;
        this.loanPort = loanPort;
        this.transferPort = transferPort;
        this.operationLogPort = operationLogPort;
    }

    public Customer findCustomer(String userIdentification, String customerIdentification) throws BusinessException {

        validateInternalAnalyst(userIdentification);

        // RN-34:
        // El analista interno tiene acceso amplio a la información de clientes.
        Customer customer = customerPort.findByIdentificationNumber(customerIdentification);
        if (customer == null) {
            throw new BusinessException("No existe un cliente con esa identificación");
        }

        return customer;
    }

    public BankAccount findAccount(String userIdentification, String accountNumber) throws BusinessException {

        validateInternalAnalyst(userIdentification);

        // RN-34:
        // El analista interno tiene acceso amplio a la información de cuentas.
        BankAccount account = accountPort.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new BusinessException("No existe una cuenta con ese número");
        }

        return account;
    }

    public Loan findLoan(String userIdentification, String loanId) throws BusinessException {

        validateInternalAnalyst(userIdentification);

        // RN-34:
        // El analista interno tiene acceso amplio a la información de préstamos.
        Loan loan = loanPort.findByLoanId(loanId);
        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        return loan;
    }

    public Transfer findTransfer(String userIdentification, int transferId) throws BusinessException {

        validateInternalAnalyst(userIdentification);

        // RN-34:
        // El analista interno tiene acceso amplio a la información de transferencias.
        Transfer transfer = transferPort.findByTransferId(transferId);
        if (transfer == null) {
            throw new BusinessException("No existe una transferencia con ese ID");
        }

        return transfer;
    }

    public List<OperationLog> findAllLogs(String userIdentification) throws BusinessException {

        validateInternalAnalyst(userIdentification);

        // RN-36:
        // El analista interno tiene acceso completo de consulta a la bitácora.
        return operationLogPort.findAll();
    }

    private User validateInternalAnalyst(String userIdentification) {

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Validación adicional:
        // El analista debe estar activo.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El analista interno debe estar activo para consultar esta información");
        }

        // RN-34 / RN-36 / RN-AD10:
        // Solo el analista interno puede usar este caso de consulta amplia.
        if (user.getSystemRole() != RoleType.INTERNAL_ANALYST) {
            throw new BusinessException("Solo un analista interno puede acceder a esta información");
        }

        return user;
    }
}
