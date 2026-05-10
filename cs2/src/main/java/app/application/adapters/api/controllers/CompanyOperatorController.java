package app.application.adapters.api.controllers;

import app.application.adapters.api.request.TransferRequest;
import app.application.adapters.api.response.TransferDetailResponse;
import app.application.adapters.api.response.TransferResponse;
import app.application.usecases.CompanyOperatorUseCase;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/company-operator")
public class CompanyOperatorController {

    private final CompanyOperatorUseCase companyOperatorUseCase;

    public CompanyOperatorController(CompanyOperatorUseCase companyOperatorUseCase) {
        this.companyOperatorUseCase = companyOperatorUseCase;
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        Transfer transfer = toTransfer(request);

        companyOperatorUseCase.createTransfer(
                authenticatedUser.getIdentificationNumber(),
                transfer
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toTransferResponse(transfer));
    }

    private static Transfer toTransfer(TransferRequest request) {
        Transfer transfer = new Transfer();

        transfer.setAmount(request.getAmount());
        transfer.setTransferType(request.getTransferType());

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber(request.getSourceAccountNumber());
        transfer.setSourceAccount(sourceAccount);

        if (request.getTargetAccountNumber() != null &&
                !request.getTargetAccountNumber().trim().isEmpty()) {
            BankAccount targetAccount = new BankAccount();
            targetAccount.setAccountNumber(request.getTargetAccountNumber());
            transfer.setTargetAccount(targetAccount);
        }

        return transfer;
    }

    private static TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getTransferId(),
                transfer.getSourceAccount() != null ? transfer.getSourceAccount().getAccountNumber() : null,
                transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null,
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getTransferType(),
                transfer.getCreationDate(),
                transfer.getExpirationDate(),
                transfer.getCreatedBy() != null ? transfer.getCreatedBy().getIdentificationNumber() : null
        );
    }
    @GetMapping("/transfers")
    public ResponseEntity<List<TransferDetailResponse>> findTransfers(
            @AuthenticationPrincipal User authenticatedUser) {

        List<Transfer> transfers = companyOperatorUseCase.findTransfers(
                authenticatedUser.getIdentificationNumber()
        );

        return ResponseEntity.ok(
                transfers.stream()
                        .map(CompanyOperatorController::toTransferDetailResponse)
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

    @GetMapping("/transfers/{transferId}")
    public ResponseEntity<TransferDetailResponse> findTransfer(
            @PathVariable int transferId,
            @AuthenticationPrincipal User authenticatedUser) {

        Transfer transfer = companyOperatorUseCase.findTransfer(
                authenticatedUser.getIdentificationNumber(),
                transferId
        );

        return ResponseEntity.ok(toTransferDetailResponse(transfer));
    }
}