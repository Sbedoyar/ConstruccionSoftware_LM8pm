package app.application.adapters.api.controllers;

import app.application.adapters.api.request.ApproveTransferRequest;
import app.application.adapters.api.request.DelegateCompanyOperatorRequest;
import app.application.adapters.api.request.RejectTransferRequest;
import app.application.adapters.api.response.ApproveTransferResponse;
import app.application.adapters.api.response.DelegateCompanyOperatorResponse;
import app.application.adapters.api.response.RejectTransferResponse;
import app.application.adapters.api.response.TransferDetailResponse;
import app.application.usecases.CompanySupervisorUseCase;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @Valid @RequestBody ApproveTransferRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        companySupervisorUseCase.approveTransfer(
                request.getTransferId(),
                authenticatedUser.getIdentificationNumber()
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
            @Valid @RequestBody RejectTransferRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        companySupervisorUseCase.rejectTransfer(
                request.getTransferId(),
                authenticatedUser.getIdentificationNumber()
        );

        RejectTransferResponse response = new RejectTransferResponse(
                request.getTransferId(),
                "REJECTED",
                "Transferencia rechazada correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/users/delegate-operator")
    public ResponseEntity<DelegateCompanyOperatorResponse> delegateCompanyOperator(
            @Valid @RequestBody DelegateCompanyOperatorRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        companySupervisorUseCase.delegateCompanyOperator(
                authenticatedUser.getIdentificationNumber(),
                request.getTargetUserIdentification(),
                request.getCompanyIdentification()
        );

        DelegateCompanyOperatorResponse response = new DelegateCompanyOperatorResponse(
                request.getTargetUserIdentification(),
                request.getCompanyIdentification(),
                "COMPANY_OPERATOR",
                "Usuario delegado como operador de empresa correctamente"
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/transfers/pending")
        public ResponseEntity<List<TransferDetailResponse>> findPendingTransfers(
                @AuthenticationPrincipal User authenticatedUser) {

        List<Transfer> transfers = companySupervisorUseCase.findPendingTransfers(
                authenticatedUser.getIdentificationNumber()
        );

        return ResponseEntity.ok(
                transfers.stream()
                        .map(CompanySupervisorController::toTransferDetailResponse)
                        .toList()
        );
        }
        private static TransferDetailResponse toTransferDetailResponse(Transfer transfer) {
                return new TransferDetailResponse(
                        transfer.getTransferId(),
                        transfer.getSourceAccount() != null ? transfer.getSourceAccount().getAccountNumber() : null,
                        transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null,
                        transfer.getAmount(),
                        transfer.getExpirationDate(),
                        transfer.getStatus(),
                        transfer.getCreatedBy() != null ? transfer.getCreatedBy().getIdentificationNumber() : null,
                        transfer.getCreationDate(),
                        transfer.getReviewedBy() != null ? transfer.getReviewedBy().getIdentificationNumber() : null,
                        transfer.getReviewDate(),
                        transfer.getTransferType()
                );
                }
}