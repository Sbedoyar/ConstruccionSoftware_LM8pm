package app.application.adapters.api.response;

import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Map;

public record OperationLogResponse(
        String logId,
        OperationType operationType,
        LocalDateTime timestamp,
        String userIdentification,
        RoleType userRole,
        String affectedProductId,
        Map<String, Object> detailData
) {}