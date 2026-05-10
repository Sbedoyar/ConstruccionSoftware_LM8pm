package app.application.adapters.api.response;

import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerTransferResponse(
        Integer transferId,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        TransferType transferType,
        TransferStatus status,
        LocalDateTime creationDate,
        String message
) {}