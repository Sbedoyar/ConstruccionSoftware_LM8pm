package app.application.adapters.api.response;

public record DisburseLoanResponse(
        String loanId,
        String disbursementAccountNumber,
        String loanStatus,
        String message
) {}