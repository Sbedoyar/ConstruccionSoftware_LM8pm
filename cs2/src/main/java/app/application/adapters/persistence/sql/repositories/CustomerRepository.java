package app.application.adapters.persistence.sql.repositories;

import app.application.adapters.persistence.sql.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    CustomerEntity findByIdentificationNumber(String identificationNumber);
}