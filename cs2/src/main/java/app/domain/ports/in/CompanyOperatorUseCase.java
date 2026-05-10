package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.transfer.Transfer;

import java.util.List;

public interface CompanyOperatorUseCase {

    void createTransfer(String userIdentification,
                        Transfer transfer) throws BusinessException;

    List<Transfer> findTransfers(String operatorIdentification) throws BusinessException;

    Transfer findTransfer(String operatorIdentification,
                          int transferId) throws BusinessException;
}