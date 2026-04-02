package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.AccountStatus;
import org.springframework.stereotype.Service;

@Service
public class ValidateAccountOperation {

    public void validateAccountOperability(BankAccount account) throws BusinessException {

        // Validación general:
        // La cuenta no puede ser null.
        if (account == null) {
            throw new BusinessException("La cuenta no puede ser null");
        }

        // RN-05:
        // No se permiten operaciones en cuentas bloqueadas o canceladas,
        // salvo procesos internos de cierre.
        if (account.getAccountStatus() == AccountStatus.BLOCKED ||
            account.getAccountStatus() == AccountStatus.CANCELLED) {
            throw new BusinessException("No se permiten operaciones sobre cuentas bloqueadas o canceladas");
        }
    }
}