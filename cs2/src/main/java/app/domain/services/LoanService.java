package app.domain.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.AccountPort;
import app.domain.ports.LoanPort;
import app.domain.ports.OperationLogPort;

public class LoanService {

    private final LoanPort loanPort;
    private final AccountPort accountPort;
    private final AccountService accountService;
    private final OperationLogPort logPort;
    private final EmployeeAccessService employeeAccessService;

    public LoanService(LoanPort loanPort, AccountPort accountPort, AccountService accountService, OperationLogPort logPort, EmployeeAccessService employeeAccessService ) {
        this.loanPort = loanPort;
        this.accountPort = accountPort;
        this.accountService = accountService;
        this.logPort = logPort;
        this.employeeAccessService = employeeAccessService;
    }

    public void createLoan(Loan loan, Customer customer) {
    
        // RN6: Todo préstamo debe estar asociado a 
        // un ID_Cliente_Solicitante que sea válido y activo

        if (customer == null) {
            throw new BusinessExceptions("El cliente no existe.");
        }
    
        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessExceptions("El cliente no está activo.");
        }

        loan.setCustomer(customer);
        loanPort.save(loan);
    }


    public void reviewLoan(Loan loan, LoanStatus newStatus, User reviewer) {

        //RN28: Puede Consultar estado pero no modificarlo
        employeeAccessService.validateLoanModification(reviewer);
    
        // RN7: Un préstamo solo puede pasar de
        // "En estudio" a "Aprobado" o "Rechazado".

        if (loan.getLoanStatus() != LoanStatus.IN_REVIEW) {
            throw new BusinessExceptions("El préstamo no está en revisión");
        }
    
        // RN7: solo a aprobado o rechazado
        if (newStatus != LoanStatus.APPROVED &&
            newStatus != LoanStatus.REJECTED) {
    
            throw new BusinessExceptions("Estado no válido");
        }
    
        // RN8: Solo el Analista Interno 
        // puede realizar la aprobación o rechazo

        if (reviewer.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessExceptions("El usuario no está activo");
        }
        
        if (reviewer.getSystemRole() != RoleType.INTERNAL_ANALYST) {
            throw new BusinessExceptions("Solo un analista interno puede aprobar o rechazar");
        }
        loan.setLoanStatus(newStatus);
        loan.setReviewedBy(reviewer);
        loan.setReviewDate(LocalDate.now());
           
        loanPort.save(loan);
    }


    public void disburseLoan(Loan loan, User user) {
    
        // RN9: El paso a "Desembolsado" 
        // solo es posible desde el estado "Aprobado"

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new BusinessExceptions("El préstamo no está aprobado");
        }
    
        // RN10: No se puede marcar un préstamo como "Desembolsado" 
        // sin que se haya definido y validado la Cuenta_Destino_Desembolso 
        // (debe ser una cuenta activa del cliente).

        BankAccount account  = loan.getDisbursementAccount();
    
        if (account  == null) {
            throw new BusinessExceptions("No se ha definido cuenta de desembolso");
        }
    
        // reutilizas tu regla RN5 🔥
        accountService.validateAccountActive(account);
    
        // RN11: Se debe validar que 
        // el Monto_Aprobado sea mayor a cero.

        if (loan.getApprovedAmount() == null || 
            loan.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessExceptions("Monto inválido");
        }

        LoanStatus previousStatus = loan.getLoanStatus();
    
        BigDecimal previousBalance = account.getBalance();

        // RN12: El Saldo_Actual de la cuenta destino 
        // debe aumentar en el Monto_Aprobado.

        account.setBalance(
            account.getBalance().add(loan.getApprovedAmount())
        );
    
        // RN9: cambiar estado
        loan.setLoanStatus(LoanStatus.DISBURSED);

        loan.setDisbursementDate(LocalDate.now());
    
        // guardar cambios
        accountPort.save(account);
        loanPort.save(loan);
    
        // RN13: bitácora (simulado por ahora)
        OperationLog log = new OperationLog();
        
        log.setOperationType(OperationType.LOAN_DISBURSED);
        log.setTimestamp(LocalDateTime.now());
        log.setUser(user); // o el usuario que ejecuta
        log.setUserRole(user.getSystemRole());
        log.setAffectedProductId(loan.getProductCode());

        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus.name());
        details.put("newStatus", loan.getLoanStatus().name());

        details.put("balanceBefore", previousBalance);
        details.put("balanceAfter", account.getBalance());

        details.put("amount", loan.getApprovedAmount());
        details.put("destinationAccount", account.getAccountNumber());
        
        log.setDetailData(details);
        
        logPort.save(log);
    }


}

