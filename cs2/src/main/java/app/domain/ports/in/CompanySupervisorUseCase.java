package app.domain.ports.in;

import app.domain.exceptions.BusinessException;

public interface CompanySupervisorUseCase {

    void approveTransfer(Integer transferId,
                         String supervisorIdentification) throws BusinessException;

    void rejectTransfer(Integer transferId,
                        String supervisorIdentification) throws BusinessException;

    void delegateCompanyOperator(String delegatorIdentification,
                             String targetUserIdentification,
                             String companyIdentification) throws BusinessException;
}
