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
}