package app.application.adapters.persistence.sql.repositories;

import app.application.adapters.persistence.sql.entities.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<TransferEntity, Integer> {

    TransferEntity findByTransferId(int transferId);

    List<TransferEntity> findByStatusAndExpirationDateBefore(String status, LocalDateTime now);
}