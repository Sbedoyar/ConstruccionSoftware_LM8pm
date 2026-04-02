package app.domain.models.bankingProduct;

import java.math.BigDecimal;
import java.time.LocalDate;

//import app.domain.exceptions.BusinessExceptions;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.CurrencyType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public class BankAccount extends BankingProduct{
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance = BigDecimal.ZERO;
    private CurrencyType currency;
    private AccountStatus accountStatus;
    private LocalDate openingDate;

    /*public boolean isOperative() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.balance.compareTo(amount) >= 0;
    }

    public void credit(BigDecimal amount) {
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount);

        if (!isOperative()) {
            throw new BusinessExceptions("Cannot debit a blocked or cancelled account");
        }

        if (!hasSufficientFunds(amount)) {
            throw new BusinessExceptions("Insufficient funds");
        }

        this.balance = this.balance.subtract(amount);
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessExceptions("Amount must be greater than zero");
        }
    }
    /* */

    public boolean internal;

}
