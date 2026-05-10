package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.transfer.Transfer;
import app.domain.services.CreateLoan;
import app.domain.services.CreateTransfer;
import app.domain.services.FindCustomerAccount;
import app.domain.services.FindCustomerHistory;
import app.domain.services.FindCustomerLoan;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerUseCase implements app.domain.ports.in.CustomerUseCase {

    private final FindCustomerHistory findCustomerHistory;
    private final CreateTransfer createTransfer;
    private final FindCustomerAccount findCustomerAccount;
    private final CreateLoan createLoan;
    private final FindCustomerLoan findCustomerLoan;

    public CustomerUseCase(FindCustomerHistory findCustomerHistory,
                           CreateTransfer createTransfer,
                           FindCustomerAccount findCustomerAccount,
                           CreateLoan createLoan,
                           FindCustomerLoan findCustomerLoan) {
        this.findCustomerHistory = findCustomerHistory;
        this.createTransfer = createTransfer;
        this.findCustomerAccount = findCustomerAccount;
        this.createLoan = createLoan;
        this.findCustomerLoan = findCustomerLoan;
    }

    @Override
    public List<OperationLog> findHistoryByProduct(String userIdentification,
                                                   String affectedProductId) throws BusinessException {
        return findCustomerHistory.findHistoryByProduct(userIdentification, affectedProductId);
    }

    @Override
    public void createTransfer(String userIdentification,
                               Transfer transfer) throws BusinessException {
        createTransfer.createTransfer(userIdentification, transfer);
    }

    @Override
    public BankAccount findAccount(String userIdentification,
                                   String accountNumber) throws BusinessException {
        return findCustomerAccount.findAccount(userIdentification, accountNumber);
    }

    @Override
    public void createLoan(String customerIdentification,
                           String userIdentification,
                           Loan loan) throws BusinessException {
        createLoan.createLoan(customerIdentification, userIdentification, loan);
    }
    @Override
    public Loan findLoan(String userIdentification,
                        String loanId) throws BusinessException {
        return findCustomerLoan.findLoan(userIdentification, loanId);
    }
}