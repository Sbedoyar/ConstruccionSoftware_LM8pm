package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.transfer.Transfer;
import app.domain.services.CreateTransfer;
import app.domain.services.FindCompanyOperatorTransfer;
import app.domain.services.FindCompanyOperatorTransfers;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyOperatorUseCase implements app.domain.ports.in.CompanyOperatorUseCase {

    private final CreateTransfer createTransfer;
    private final FindCompanyOperatorTransfers findCompanyOperatorTransfers;
    private final FindCompanyOperatorTransfer findCompanyOperatorTransfer;

    public CompanyOperatorUseCase(CreateTransfer createTransfer,
                                  FindCompanyOperatorTransfers findCompanyOperatorTransfers,
                                  FindCompanyOperatorTransfer findCompanyOperatorTransfer) {
        this.createTransfer = createTransfer;
        this.findCompanyOperatorTransfers = findCompanyOperatorTransfers;
        this.findCompanyOperatorTransfer = findCompanyOperatorTransfer;
    }

    @Override
    public void createTransfer(String userIdentification,
                               Transfer transfer) throws BusinessException {
        createTransfer.createTransfer(userIdentification, transfer);
    }

    @Override
    public List<Transfer> findTransfers(String operatorIdentification) throws BusinessException {
        return findCompanyOperatorTransfers.findTransfers(operatorIdentification);
    }

    @Override
    public Transfer findTransfer(String operatorIdentification,
                                 int transferId) throws BusinessException {
        return findCompanyOperatorTransfer.findTransfer(operatorIdentification, transferId);
    }
}