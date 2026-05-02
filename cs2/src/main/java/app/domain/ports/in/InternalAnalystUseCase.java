package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.operationLog.OperationLog;

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
}