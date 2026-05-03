package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;

public interface TellerUseCase {

    BankAccount findAccount(String tellerIdentification,
                            String accountNumber) throws BusinessException;

    void createAccount(String customerIdentification,
                       String tellerIdentification,
                       BankAccount account) throws BusinessException;
}