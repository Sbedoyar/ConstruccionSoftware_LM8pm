package app.application.adapters.api.response;

public record DelegateCompanyOperatorResponse(
        String targetUserIdentification,
        String companyIdentification,
        String newRole,
        String message
) {}