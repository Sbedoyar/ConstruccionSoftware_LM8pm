package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.transfer.Transfer;
import app.domain.services.ApproveLoan;
import app.domain.services.DisburseLoan;
import app.domain.services.FindDataForInternalAnalyst;
import app.domain.services.RejectLoan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InternalAnalystUseCase implements app.domain.ports.in.InternalAnalystUseCase {

    @Autowired
    private ApproveLoan approveLoan;

    @Autowired
    private RejectLoan rejectLoan;

    @Autowired
    private DisburseLoan disburseLoan;

    @Autowired
    private FindDataForInternalAnalyst findDataForInternalAnalyst;

    public InternalAnalystUseCase(ApproveLoan approveLoan,
                                  RejectLoan rejectLoan,
                                  DisburseLoan disburseLoan,
                                  FindDataForInternalAnalyst findDataForInternalAnalyst) {
        this.approveLoan = approveLoan;
        this.rejectLoan = rejectLoan;
        this.disburseLoan = disburseLoan;
        this.findDataForInternalAnalyst = findDataForInternalAnalyst;
    }

    @Override
    public void approveLoan(String loanId,
                            String analystIdentification,
                            BigDecimal approvedAmount) throws BusinessException {
        approveLoan.approveLoan(loanId, analystIdentification, approvedAmount);
    }

    @Override
    public void rejectLoan(String loanId,
                           String analystIdentification,
                           String rejectionReason) throws BusinessException {
        rejectLoan.rejectLoan(loanId, analystIdentification, rejectionReason);
    }

    @Override
    public void disburseLoan(String loanId,
                             String analystIdentification,
                             String disbursementAccountNumber) throws BusinessException {
        disburseLoan.disburseLoan(loanId, analystIdentification, disbursementAccountNumber);
    }

    @Override
    public List<OperationLog> findAllLogs(String analystIdentification) throws BusinessException {
        return findDataForInternalAnalyst.findAllLogs(analystIdentification);
    }

    @Override
    public Customer findCustomer(String analystIdentification,
                                String customerIdentification) throws BusinessException {
        return findDataForInternalAnalyst.findCustomer(
                analystIdentification,
                customerIdentification
        );
    }

    @Override
    public BankAccount findAccount(String analystIdentification,
                                String accountNumber) throws BusinessException {
        return findDataForInternalAnalyst.findAccount(
                analystIdentification,
                accountNumber
        );
    }
    
    @Override
    public Loan findLoan(String analystIdentification,
                        String loanId) throws BusinessException {
        return findDataForInternalAnalyst.findLoan(
                analystIdentification,
                loanId
        );
    }

    @Override
    public Transfer findTransfer(String analystIdentification,
                                int transferId) throws BusinessException {
        return findDataForInternalAnalyst.findTransfer(
                analystIdentification,
                transferId
        );
    }
}