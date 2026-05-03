package app.application.adapters.api.controllers;

import app.application.adapters.api.response.OperationLogResponse;
import app.application.usecases.CustomerUseCase;
import app.domain.models.operationLog.OperationLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.domain.models.person.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    @GetMapping("/history/{affectedProductId}")
    public ResponseEntity<List<OperationLogResponse>> findHistoryByProduct(
            @PathVariable String affectedProductId,
            @AuthenticationPrincipal User authenticatedUser) {

        List<OperationLog> logs = customerUseCase.findHistoryByProduct(
                authenticatedUser.getIdentificationNumber(),
                affectedProductId
        );

        return ResponseEntity.ok(
                logs.stream()
                        .map(CustomerController::toOperationLogResponse)
                        .toList()
        );
    }

    private static OperationLogResponse toOperationLogResponse(OperationLog log) {
        return new OperationLogResponse(
                log.getLogId(),
                log.getOperationType(),
                log.getTimestamp(),
                log.getUser() != null ? log.getUser().getIdentificationNumber() : null,
                log.getUserRole(),
                log.getAffectedProductId(),
                log.getDetailData()
        );
    }
}