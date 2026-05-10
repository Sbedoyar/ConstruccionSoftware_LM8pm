package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.transfer.Transfer;
import app.domain.services.ApproveTransfer;
import app.domain.services.DelegateCompanyUser;
import app.domain.services.FindPendingTransfersForSupervisor;
import app.domain.services.RejectTransfer;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompanySupervisorUseCase implements app.domain.ports.in.CompanySupervisorUseCase {

    private ApproveTransfer approveTransfer;
    private RejectTransfer rejectTransfer;
    private DelegateCompanyUser delegateCompanyUser;
    private final FindPendingTransfersForSupervisor findPendingTransfersForSupervisor;

    public CompanySupervisorUseCase(ApproveTransfer approveTransfer,
                                    RejectTransfer rejectTransfer,
                                    DelegateCompanyUser delegateCompanyUser,
                                    FindPendingTransfersForSupervisor findPendingTransfersForSupervisor) {
        this.approveTransfer = approveTransfer;
        this.rejectTransfer = rejectTransfer;
        this.delegateCompanyUser = delegateCompanyUser;
        this.findPendingTransfersForSupervisor = findPendingTransfersForSupervisor;
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

    @Override
    public List<Transfer> findPendingTransfers(String supervisorIdentification) throws BusinessException {
        return findPendingTransfersForSupervisor.findPendingTransfers(supervisorIdentification);
    }
}