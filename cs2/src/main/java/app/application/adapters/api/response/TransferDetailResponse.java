package app.application.adapters.api.response;

import app.domain.models.enums.TransferStatus;
import app.domain.models.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferDetailResponse(
        Integer transferId,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        LocalDateTime expirationDate,
        TransferStatus status,
        String createdByIdentification,
        LocalDateTime creationDate,
        String reviewedByIdentification,
        LocalDateTime reviewDate,
        TransferType transferType
) {}