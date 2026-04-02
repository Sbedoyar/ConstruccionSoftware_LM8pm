package app.domain.ports;

import app.domain.models.bankingProduct.Loan;

public interface LoanPort {
    Loan findByLoanId(String loanId);
    void save(Loan loan);
    void update(Loan loan);
}
