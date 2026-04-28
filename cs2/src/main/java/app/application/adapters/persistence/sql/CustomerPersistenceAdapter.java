package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.CustomerEntity;
import app.application.adapters.persistence.sql.repositories.CustomerRepository;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.ports.CustomerPort;
import org.springframework.stereotype.Service;

@Service
public class CustomerPersistenceAdapter implements CustomerPort {

    private final CustomerRepository repository;

    public CustomerPersistenceAdapter(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer findByIdentificationNumber(String identificationNumber) {
        CustomerEntity entity = repository.findByIdentificationNumber(identificationNumber);
        return toModel(entity);
    }

    @Override
    public void save(Customer customer) {
        repository.save(toEntity(customer));
    }

    private CustomerEntity toEntity(Customer customer) {
        CustomerEntity entity = new CustomerEntity();

        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setIdentificationNumber(customer.getIdentificationNumber());
        entity.setEmail(customer.getEmail());
        entity.setPhone(customer.getPhone());
        entity.setAddress(customer.getAddress());
        entity.setRegistrationDate(customer.getRegistrationDate());

        if (customer.getCustomerStatus() != null) {
            entity.setCustomerStatus(customer.getCustomerStatus().name());
        }

        if (customer instanceof IndividualCustomer individualCustomer) {
            entity.setCustomerType("INDIVIDUAL");
            entity.setDateOfBirth(individualCustomer.getDateOfBirth());
        } else if (customer instanceof BusinessCustomer businessCustomer) {
            entity.setCustomerType("BUSINESS");

            if (businessCustomer.getLegalRepresentative() != null) {
                entity.setLegalRepresentativeName(
                        businessCustomer.getLegalRepresentative().getName()
                );
                entity.setLegalRepresentativeIdentification(
                        businessCustomer.getLegalRepresentative().getIdentificationNumber()
                );
            }
        }

        return entity;
    }

    private Customer toModel(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        Customer customer;

        if ("BUSINESS".equals(entity.getCustomerType())) {
            BusinessCustomer businessCustomer = new BusinessCustomer();

            if (entity.getLegalRepresentativeIdentification() != null ||
                entity.getLegalRepresentativeName() != null) {

                IndividualCustomer legalRepresentative = new IndividualCustomer();
                legalRepresentative.setName(entity.getLegalRepresentativeName());
                legalRepresentative.setIdentificationNumber(entity.getLegalRepresentativeIdentification());
                businessCustomer.setLegalRepresentative(legalRepresentative);
            }

            customer = businessCustomer;
        } else {
            IndividualCustomer individualCustomer = new IndividualCustomer();
            individualCustomer.setDateOfBirth(entity.getDateOfBirth());
            customer = individualCustomer;
        }

        customer.setId(entity.getId());
        customer.setName(entity.getName());
        customer.setIdentificationNumber(entity.getIdentificationNumber());
        customer.setEmail(entity.getEmail());
        customer.setPhone(entity.getPhone());
        customer.setAddress(entity.getAddress());
        customer.setRegistrationDate(entity.getRegistrationDate());

        if (entity.getCustomerStatus() != null) {
            customer.setCustomerStatus(CustomerStatus.valueOf(entity.getCustomerStatus()));
        }

        return customer;
    }
}