package app.domain.models.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.TransferStatus;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class Transfer {
    private int transferId;
    private BankAccount sourceAccount;
    private BankAccount targetAccount;
    private BigDecimal amount;
    private LocalDate expirationDate;
    private TransferStatus status;
    private Customer createdBy;
    private LocalDate creationDate;
    private User reviewedBy;
    private LocalDate reviewDate;

}
