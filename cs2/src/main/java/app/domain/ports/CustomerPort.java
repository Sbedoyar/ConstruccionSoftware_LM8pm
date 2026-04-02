package app.domain.ports;

import app.domain.models.person.Customer;

public interface CustomerPort {
    Customer findByIdentificationNumber(String identificationNumber);
    void save(Customer customer);
}
