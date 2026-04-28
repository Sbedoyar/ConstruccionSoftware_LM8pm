package app.application.usecases;

import app.domain.models.person.IndividualCustomer;
import app.domain.services.CreateCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommercialUseCase {

    @Autowired
    private CreateCustomer createCustomer;

    public CommercialUseCase(CreateCustomer createCustomer) {
        this.createCustomer = createCustomer;
    }

    public void createIndividualCustomer(IndividualCustomer customer) {
        createCustomer.createCustomer(customer);
    }
}