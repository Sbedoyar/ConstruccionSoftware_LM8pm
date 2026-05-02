package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.operationLog.OperationLog;

import java.util.List;

public interface CustomerUseCase {

    List<OperationLog> findHistoryByProduct(String userIdentification,
                                            String affectedProductId) throws BusinessException;
}