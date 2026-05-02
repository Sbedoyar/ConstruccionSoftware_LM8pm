package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.operationLog.OperationLog;
import app.domain.services.FindCustomerHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerUseCase implements app.domain.ports.in.CustomerUseCase {

    @Autowired
    private FindCustomerHistory findCustomerHistory;

    public CustomerUseCase(FindCustomerHistory findCustomerHistory) {
        this.findCustomerHistory = findCustomerHistory;
    }

    @Override
    public List<OperationLog> findHistoryByProduct(String userIdentification,
                                                   String affectedProductId) throws BusinessException {
        return findCustomerHistory.findHistoryByProduct(userIdentification, affectedProductId);
    }
}