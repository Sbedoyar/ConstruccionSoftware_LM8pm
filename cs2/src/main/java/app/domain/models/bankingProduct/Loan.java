package app.domain.models.bankingProduct;

import java.math.BigDecimal;
import java.time.LocalDate;

import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.LoanType;
import app.domain.models.person.Customer;
import app.domain.models.person.Person;
import app.domain.models.person.User;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class Loan extends BankingProduct{
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private int termMonths;
    private LoanStatus loanStatus = LoanStatus.IN_REVIEW;
    private Customer customer;
    private Person createdBy;
    private LocalDate creationDate;
    private User reviewedBy;
    private LocalDate reviewDate;
    private LocalDate disbursementDate;
    private BankAccount disbursementAccount;

}
