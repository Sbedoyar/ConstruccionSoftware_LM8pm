package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.transfer.Transfer;

import java.util.List;

public interface CustomerUseCase {

    List<OperationLog> findHistoryByProduct(String userIdentification,
                                            String affectedProductId) throws BusinessException;

    void createTransfer(String userIdentification,
                        Transfer transfer) throws BusinessException;

    BankAccount findAccount(String userIdentification,
                            String accountNumber) throws BusinessException;

    void createLoan(String customerIdentification,
                    String userIdentification,
                    Loan loan) throws BusinessException;

    Loan findLoan(String userIdentification,
                  String loanId) throws BusinessException;
}