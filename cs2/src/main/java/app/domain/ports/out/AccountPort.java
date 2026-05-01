package app.domain.ports.out;

import app.domain.models.bankingProduct.BankAccount;

public interface AccountPort {
    BankAccount findByAccountNumber (String accountNumber);
    void save(BankAccount account);
    void update(BankAccount account);
}
