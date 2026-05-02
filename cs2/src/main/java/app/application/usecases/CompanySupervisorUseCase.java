package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.services.ApproveTransfer;
import app.domain.services.RejectTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanySupervisorUseCase implements app.domain.ports.in.CompanySupervisorUseCase {

    @Autowired
    private ApproveTransfer approveTransfer;

    @Autowired
    private RejectTransfer rejectTransfer;

    public CompanySupervisorUseCase(ApproveTransfer approveTransfer,
                                    RejectTransfer rejectTransfer) {
        this.approveTransfer = approveTransfer;
        this.rejectTransfer = rejectTransfer;
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
}