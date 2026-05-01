package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.OperationType;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Service
public class DisburseLoan {

    private final LoanPort loanPort;
    private final AccountPort accountPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    //@Autowired
    public DisburseLoan(LoanPort loanPort, AccountPort accountPort, UserPort userPort, OperationLogPort operationLogPort) {
        this.loanPort = loanPort;
        this.accountPort = accountPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    //@Transactional
    public void disburseLoan(String loanId, String userIdentification, String accountNumber) throws BusinessException {

        // Validación general:
        // El ID del préstamo es obligatorio.
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Validación general:
        // El número de cuenta destino es obligatorio.
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BusinessException("El número de cuenta destino es obligatorio");
        }

        // Se busca el préstamo a desembolsar.
        Loan loan = loanPort.findByLoanId(loanId.trim());
        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        // Se busca el usuario que realiza el desembolso.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Se busca la cuenta destino del desembolso.
        BankAccount account = accountPort.findByAccountNumber(accountNumber.trim());
        if (account == null) {
            throw new BusinessException("No existe una cuenta con ese número");
        }

        validateActiveUser(user);

        // RN-35 / RN-37:
        // El analista interno es el rol primario para modificar el estado de los préstamos
        // y no puede modificar saldos arbitrariamente, solo como resultado de un flujo definido.
        validateInternalAnalyst(user);

        // RN-09:
        // El paso a "Desembolsado" solo es posible desde el estado "Aprobado".
        validateApprovedLoan(loan);

        // RN-10:
        // No se puede desembolsar sin una cuenta destino válida, activa y del mismo cliente.
        validateDisbursementAccount(loan, account);

        // RN-11:
        // El monto aprobado debe ser mayor que cero.
        validateApprovedAmount(loan);

        BigDecimal balanceBefore = account.getBalance();

        // RN-12:
        // El saldo de la cuenta destino debe aumentar en el monto aprobado.
        account.setBalance(account.getBalance().add(loan.getApprovedAmount()));

        // Se actualiza la información del préstamo.
        loan.setDisbursementAccount(account);
        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());

        // Se persisten los cambios.
        accountPort.update(account);
        loanPort.update(loan);

        // RN-13:
        // Se debe generar un registro en la bitácora.
        registerDisbursementLog(user, loan, account, balanceBefore);
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para realizar esta operación");
        }
    }

    private void validateInternalAnalyst(User user) {

        // RN-35 / RN-37:
        // Solo el analista interno puede ejecutar el desembolso dentro del flujo definido.
        if (user.getSystemRole() != RoleType.INTERNAL_ANALYST) {
            throw new BusinessException("Solo un analista interno puede desembolsar préstamos");
        }
    }

    private void validateApprovedLoan(Loan loan) {

        // RN-09:
        // Solo se puede desembolsar un préstamo en estado APPROVED.
        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new BusinessException("Solo se puede desembolsar un préstamo aprobado");
        }
    }

    private void validateDisbursementAccount(Loan loan, BankAccount account) {

        // RN-10:
        // La cuenta destino debe estar activa.
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("La cuenta destino debe estar activa");
        }

        // RN-10:
        // La cuenta destino debe pertenecer al mismo cliente del préstamo.
        if (loan.getOwner() == null || account.getOwner() == null ||
            !loan.getOwner().getIdentificationNumber().equals(account.getOwner().getIdentificationNumber())) {
            throw new BusinessException("La cuenta destino debe pertenecer al mismo cliente del préstamo");
        }
    }

    private void validateApprovedAmount(Loan loan) {

        // RN-11:
        // El monto aprobado debe ser mayor que cero.
        if (loan.getApprovedAmount() == null || loan.getApprovedAmount().signum() <= 0) {
            throw new BusinessException("El monto aprobado debe ser mayor que cero");
        }
    }

    private void registerDisbursementLog(User user,
                                         Loan loan,
                                         BankAccount account,
                                         BigDecimal balanceBefore) {

        // RN-13:
        // Registro obligatorio en la bitácora del desembolso.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.LOAN_DISBURSED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(loan.getLoanId());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("loanId", loan.getLoanId());
        detailData.put("approvedAmount", loan.getApprovedAmount());
        detailData.put("disbursementAccount", account.getAccountNumber());
        detailData.put("previousLoanStatus", LoanStatus.APPROVED.name());
        detailData.put("newLoanStatus", LoanStatus.DISBURSED.name());
        detailData.put("accountBalanceBefore", balanceBefore);
        detailData.put("accountBalanceAfter", account.getBalance());

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}
