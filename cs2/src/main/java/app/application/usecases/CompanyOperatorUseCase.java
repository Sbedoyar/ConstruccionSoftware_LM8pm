package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.transfer.Transfer;
import app.domain.services.CreateTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyOperatorUseCase implements app.domain.ports.in.CompanyOperatorUseCase {

    @Autowired
    private CreateTransfer createTransfer;

    public CompanyOperatorUseCase(CreateTransfer createTransfer) {
        this.createTransfer = createTransfer;
    }

    @Override
    public void createTransfer(String userIdentification,
                               Transfer transfer) throws BusinessException {
        createTransfer.createTransfer(userIdentification, transfer);
    }
}