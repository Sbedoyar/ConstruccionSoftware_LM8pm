package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.services.CreateAccount;
import app.domain.services.FindAccountForTeller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TellerUseCase implements app.domain.ports.in.TellerUseCase {

    @Autowired
    private FindAccountForTeller findAccountForTeller;

    @Autowired
    private CreateAccount createAccount;

    public TellerUseCase(FindAccountForTeller findAccountForTeller,
                         CreateAccount createAccount) {
        this.findAccountForTeller = findAccountForTeller;
        this.createAccount = createAccount;
    }

    @Override
    public BankAccount findAccount(String tellerIdentification,
                                   String accountNumber) throws BusinessException {
        return findAccountForTeller.findAccount(tellerIdentification, accountNumber);
    }

    @Override
    public void createAccount(String customerIdentification,
                              String tellerIdentification,
                              BankAccount account) throws BusinessException {
        createAccount.createAccount(customerIdentification, tellerIdentification, account);
    }
}