package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;

public interface CommercialUseCase {
    void createIndividualCustomer(IndividualCustomer customer) throws BusinessException;

    void createBusinessCustomer(BusinessCustomer customer) throws BusinessException;
    
    void createAccount(String customerIdentification,
                   String userIdentification,
                   BankAccount account) throws BusinessException;

    void createLoan(String customerIdentification,
                    String userIdentification,
                    Loan loan) throws BusinessException;
                    
    Customer findAssignedCustomer(String userIdentification,
                              String customerIdentification) throws BusinessException;
}
