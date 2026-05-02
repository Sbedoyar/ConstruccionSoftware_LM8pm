package app.application.adapters.persistence.sql.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "loans")
public class LoanEntity {

    @Id
    @Column(name = "loan_id", nullable = false, unique = true)
    private String loanId;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "loan_status", nullable = false)
    private String loanStatus;

    @Column(name = "owner_identification", nullable = false)
    private String ownerIdentification;

    @Column(name = "created_by_identification", nullable = false)
    private String createdByIdentification;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "reviewed_by_identification")
    private String reviewedByIdentification;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "disbursement_account_number")
    private String disbursementAccountNumber;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @Column(name = "catalog_active", nullable = false)
    private boolean catalogActive;
}