package app.domain.ports.out;

import app.domain.models.person.Customer;

public interface CustomerPort {
    Customer findByIdentificationNumber(String identificationNumber);
    void save(Customer customer);
}
