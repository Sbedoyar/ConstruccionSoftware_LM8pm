package app.application.adapters.api.response;

import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanDetailResponse(
        String loanId,
        LoanType loanType,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        BigDecimal interestRate,
        int termMonths,
        LoanStatus loanStatus,
        LocalDate creationDate,
        String ownerIdentification,
        String createdByIdentification,
        String reviewedByIdentification,
        LocalDate reviewDate,
        LocalDate disbursementDate,
        String disbursementAccountNumber,
        String productCode,
        String productName,
        String productDescription
) {}