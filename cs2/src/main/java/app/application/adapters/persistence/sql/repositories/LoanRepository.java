package app.application.adapters.persistence.sql.repositories;

import app.application.adapters.persistence.sql.entities.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanEntity, String> {

    LoanEntity findByLoanId(String loanId);
}