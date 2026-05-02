package app.application.adapters.api.response;

import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        int transferId,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        TransferStatus status,
        TransferType transferType,
        LocalDateTime creationDate,
        LocalDateTime expirationDate,
        String createdByIdentification
) {}