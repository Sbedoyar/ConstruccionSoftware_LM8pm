package app.application.adapters.api.controllers;

import app.application.adapters.api.request.ApproveLoanRequest;
import app.application.adapters.api.request.DisburseLoanRequest;
import app.application.adapters.api.request.RejectLoanRequest;
import app.application.adapters.api.response.ApproveLoanResponse;
import app.application.adapters.api.response.DisburseLoanResponse;
import app.application.adapters.api.response.OperationLogResponse;
import app.application.adapters.api.response.RejectLoanResponse;
import app.application.usecases.InternalAnalystUseCase;
import app.domain.models.operationLog.OperationLog;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal-analyst")
public class InternalAnalystController {

    private final InternalAnalystUseCase internalAnalystUseCase;

    public InternalAnalystController(InternalAnalystUseCase internalAnalystUseCase) {
        this.internalAnalystUseCase = internalAnalystUseCase;
    }

    @PostMapping("/loans/approve")
    public ResponseEntity<ApproveLoanResponse> approveLoan(
            @Valid @RequestBody ApproveLoanRequest request) {

        internalAnalystUseCase.approveLoan(
                request.getLoanId(),
                request.getAnalystIdentification(),
                request.getApprovedAmount()
        );

        ApproveLoanResponse response = new ApproveLoanResponse(
                request.getLoanId(),
                request.getApprovedAmount(),
                "APPROVED",
                "Préstamo aprobado correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/loans/reject")
    public ResponseEntity<RejectLoanResponse> rejectLoan(
            @Valid @RequestBody RejectLoanRequest request) {

        internalAnalystUseCase.rejectLoan(
                request.getLoanId(),
                request.getAnalystIdentification(),
                request.getRejectionReason()
        );

        RejectLoanResponse response = new RejectLoanResponse(
                request.getLoanId(),
                "REJECTED",
                request.getRejectionReason(),
                "Préstamo rechazado correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/loans/disburse")
    public ResponseEntity<DisburseLoanResponse> disburseLoan(
            @Valid @RequestBody DisburseLoanRequest request) {

        internalAnalystUseCase.disburseLoan(
                request.getLoanId(),
                request.getAnalystIdentification(),
                request.getDisbursementAccountNumber()
        );

        DisburseLoanResponse response = new DisburseLoanResponse(
                request.getLoanId(),
                request.getDisbursementAccountNumber(),
                "DISBURSED",
                "Préstamo desembolsado correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<OperationLogResponse>> findAllLogs(
            @RequestParam String analystIdentification) {

        List<OperationLog> logs = internalAnalystUseCase.findAllLogs(analystIdentification);

        List<OperationLogResponse> response = logs.stream()
                .map(InternalAnalystController::toOperationLogResponse)
                .toList();

        return ResponseEntity.ok(response);
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