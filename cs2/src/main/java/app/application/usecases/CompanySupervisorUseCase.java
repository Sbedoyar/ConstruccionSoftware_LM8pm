package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.services.ApproveTransfer;
import app.domain.services.DelegateCompanyUser;
import app.domain.services.RejectTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanySupervisorUseCase implements app.domain.ports.in.CompanySupervisorUseCase {

    @Autowired
    private ApproveTransfer approveTransfer;

    @Autowired
    private RejectTransfer rejectTransfer;

    @Autowired
    private DelegateCompanyUser delegateCompanyUser;

    public CompanySupervisorUseCase(ApproveTransfer approveTransfer,
                                    RejectTransfer rejectTransfer,
                                    DelegateCompanyUser delegateCompanyUser) {
        this.approveTransfer = approveTransfer;
        this.rejectTransfer = rejectTransfer;
        this.delegateCompanyUser = delegateCompanyUser;
    }

    @Override
    public void approveTransfer(Integer transferId,
                                String supervisorIdentification) throws BusinessException {
        approveTransfer.approveTransfer(transferId, supervisorIdentification);
    }

    @Override
    public void rejectTransfer(Integer transferId,
                               String supervisorIdentification) throws BusinessException {
        rejectTransfer.rejectTransfer(transferId, supervisorIdentification);
    }

    @Override
    public void delegateCompanyOperator(String delegatorIdentification,
                                        String targetUserIdentification,
                                        String companyIdentification) throws BusinessException {
        delegateCompanyUser.delegateCompanyOperator(
                delegatorIdentification,
                targetUserIdentification,
                companyIdentification
        );
    }
}