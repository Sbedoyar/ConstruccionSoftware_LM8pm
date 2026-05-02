package app.application.adapters.api.response;

public record RejectTransferResponse(
        Integer transferId,
        String status,
        String message
) {}