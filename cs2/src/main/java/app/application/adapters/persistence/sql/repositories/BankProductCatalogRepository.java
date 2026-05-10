package app.application.adapters.persistence.sql.repositories;

import app.application.adapters.persistence.sql.entities.BankProductCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankProductCatalogRepository extends JpaRepository<BankProductCatalogEntity, String> {

    BankProductCatalogEntity findByProductCode(String productCode);
}