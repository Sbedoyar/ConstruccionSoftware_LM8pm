package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.transfer.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface InternalAnalystUseCase {

    void approveLoan(String loanId,
                     String analystIdentification,
                     BigDecimal approvedAmount) throws BusinessException;

    void rejectLoan(String loanId,
                    String analystIdentification,
                    String rejectionReason) throws BusinessException;

    void disburseLoan(String loanId,
                      String analystIdentification,
                      String disbursementAccountNumber) throws BusinessException;

    List<OperationLog> findAllLogs(String analystIdentification) throws BusinessException;

    Customer findCustomer(String analystIdentification,
                          String customerIdentification) throws BusinessException;

    BankAccount findAccount(String analystIdentification,
                            String accountNumber) throws BusinessException;

    Loan findLoan(String analystIdentification,
              String loanId) throws BusinessException;

    Transfer findTransfer(String analystIdentification,
                      int transferId) throws BusinessException;
}