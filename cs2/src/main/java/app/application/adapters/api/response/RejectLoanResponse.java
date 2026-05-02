package app.application.adapters.api.response;

public record RejectLoanResponse(
        String loanId,
        String loanStatus,
        String rejectionReason,
        String message
) {}