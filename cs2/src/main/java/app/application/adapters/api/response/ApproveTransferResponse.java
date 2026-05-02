package app.application.adapters.api.response;

public record ApproveTransferResponse(
        Integer transferId,
        String status,
        String message
) {}