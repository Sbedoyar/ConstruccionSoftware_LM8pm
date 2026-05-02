package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.services.CreateAccount;
import app.domain.services.CreateCustomer;
import app.domain.services.CreateLoan;
import app.domain.services.FindAssignedCustomer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommercialUseCase implements app.domain.ports.in.CommercialUseCase {

    @Autowired
    private CreateCustomer createCustomer;

    @Autowired
    private CreateAccount createAccount;

    @Autowired
    private CreateLoan createLoan;

    @Autowired
    private FindAssignedCustomer findAssignedCustomer;

    public CommercialUseCase(CreateCustomer createCustomer,
                             CreateAccount createAccount,
                             CreateLoan createLoan,
                             FindAssignedCustomer findAssignedCustomer) {
        this.createCustomer = createCustomer;
        this.createAccount = createAccount;
        this.createLoan = createLoan;
        this.findAssignedCustomer = findAssignedCustomer;
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

    @Override
    public void createLoan(String customerIdentification,
                        String userIdentification,
                        Loan loan) throws BusinessException {
        createLoan.createLoan(customerIdentification, userIdentification, loan);
    }

    @Override
    public Customer findAssignedCustomer(String userIdentification,
                                        String customerIdentification) throws BusinessException {
        return findAssignedCustomer.findAssignedCustomer(userIdentification, customerIdentification);
    }


}