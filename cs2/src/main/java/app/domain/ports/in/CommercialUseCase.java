package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.IndividualCustomer;

public interface CommercialUseCase {
    void createIndividualCustomer(IndividualCustomer customer) throws BusinessException;

    void createBusinessCustomer(BusinessCustomer customer) throws BusinessException;
    
    void createAccount(String customerIdentification,
                   String userIdentification,
                   BankAccount account) throws BusinessException;
}