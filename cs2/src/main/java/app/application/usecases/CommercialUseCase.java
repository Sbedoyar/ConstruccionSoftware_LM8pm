package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.IndividualCustomer;
import app.domain.services.CreateAccount;
import app.domain.services.CreateCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommercialUseCase implements app.domain.ports.in.CommercialUseCase {

    @Autowired
    private CreateCustomer createCustomer;

    @Autowired
    private CreateAccount createAccount;

    public CommercialUseCase(CreateCustomer createCustomer,
                             CreateAccount createAccount) {
        this.createCustomer = createCustomer;
        this.createAccount = createAccount;
    }

    @Override
    public void createIndividualCustomer(IndividualCustomer customer) throws BusinessException {
        createCustomer.createCustomer(customer);
    }

    @Override
    public void createBusinessCustomer(BusinessCustomer customer)  throws BusinessException {
        createCustomer.createCustomer(customer);
    }

    @Override
    public void createAccount(String customerIdentification,
                              String userIdentification,
                              BankAccount account)  throws BusinessException{
        createAccount.createAccount(customerIdentification, userIdentification, account);
    }
}