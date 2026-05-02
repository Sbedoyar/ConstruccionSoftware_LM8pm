package app.application.adapters.api.controllers;

import app.application.adapters.api.request.ApproveTransferRequest;
import app.application.adapters.api.request.RejectTransferRequest;
import app.application.adapters.api.response.ApproveTransferResponse;
import app.application.adapters.api.response.RejectTransferResponse;
import app.application.usecases.CompanySupervisorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company-supervisor")
public class CompanySupervisorController {

    private final CompanySupervisorUseCase companySupervisorUseCase;

    public CompanySupervisorController(CompanySupervisorUseCase companySupervisorUseCase) {
        this.companySupervisorUseCase = companySupervisorUseCase;
    }

    @PostMapping("/transfers/approve")
    public ResponseEntity<ApproveTransferResponse> approveTransfer(
            @Valid @RequestBody ApproveTransferRequest request) {

        companySupervisorUseCase.approveTransfer(
                request.getTransferId(),
                request.getSupervisorIdentification()
        );

        ApproveTransferResponse response = new ApproveTransferResponse(
                request.getTransferId(),
                "EXECUTED",
                "Transferencia aprobada y ejecutada correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/transfers/reject")
    public ResponseEntity<RejectTransferResponse> rejectTransfer(
            @Valid @RequestBody RejectTransferRequest request) {

        companySupervisorUseCase.rejectTransfer(
                request.getTransferId(),
                request.getSupervisorIdentification()
        );

        RejectTransferResponse response = new RejectTransferResponse(
                request.getTransferId(),
                "REJECTED",
                "Transferencia rechazada correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}