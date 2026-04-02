package app.domain.ports;

import app.domain.models.bankingProduct.Loan;

public interface LoanPort {

    void save(Loan loan);
    Loan findById(Long id);

}
