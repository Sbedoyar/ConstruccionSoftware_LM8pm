package app.domain.ports;

import app.domain.models.bankingProduct.BankAccount;

public interface AccountPort {

    BankAccount findByAccountNumber (String accountNumber);
    void save(BankAccount account);

}
