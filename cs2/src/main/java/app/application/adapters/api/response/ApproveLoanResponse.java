package app.application.adapters.api.response;

import java.math.BigDecimal;

public record ApproveLoanResponse(
        String loanId,
        BigDecimal approvedAmount,
        String loanStatus,
        String message
) {}