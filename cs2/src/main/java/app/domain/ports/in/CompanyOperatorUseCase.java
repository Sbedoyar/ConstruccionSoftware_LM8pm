package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.transfer.Transfer;

public interface CompanyOperatorUseCase {

    void createTransfer(String userIdentification,
                        Transfer transfer) throws BusinessException;
}