package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.operationLog.OperationLog;
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
}