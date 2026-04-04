package app.domain.models.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;
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
    private LocalDateTime expirationDate;
    private TransferStatus status;
    private User createdBy;
    private LocalDateTime creationDate;
    private User reviewedBy;
    private LocalDate reviewDate;
    private TransferType transferType;
}
