package app.application.adapters.api.controllers;

import app.application.adapters.api.request.CustomerLoanRequest;
import app.application.adapters.api.request.TransferRequest;
import app.application.adapters.api.response.AccountDetailResponse;
import app.application.adapters.api.response.CustomerTransferResponse;
import app.application.adapters.api.response.LoanDetailResponse;
import app.application.adapters.api.response.LoanResponse;
import app.application.adapters.api.response.OperationLogResponse;
import app.application.usecases.CustomerUseCase;
import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
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

    @PostMapping("/transfers")
    public ResponseEntity<CustomerTransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        Transfer transfer = toTransfer(request);

        customerUseCase.createTransfer(
                authenticatedUser.getIdentificationNumber(),
                transfer
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toCustomerTransferResponse(transfer));
    }

    private static Transfer toTransfer(TransferRequest request) {
        Transfer transfer = new Transfer();

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber(request.getSourceAccountNumber());
        transfer.setSourceAccount(sourceAccount);

        if (request.getTargetAccountNumber() != null &&
                !request.getTargetAccountNumber().trim().isEmpty()) {
            BankAccount targetAccount = new BankAccount();
            targetAccount.setAccountNumber(request.getTargetAccountNumber());
            transfer.setTargetAccount(targetAccount);
        }

        if (request.getAmount() != null) {
            transfer.setAmount(request.getAmount());
        } else {
            transfer.setAmount(BigDecimal.ZERO);
        }

        transfer.setTransferType(request.getTransferType());

        return transfer;
    }

    private static CustomerTransferResponse toCustomerTransferResponse(Transfer transfer) {
        return new CustomerTransferResponse(
                transfer.getTransferId(),
                transfer.getSourceAccount() != null ? transfer.getSourceAccount().getAccountNumber() : null,
                transfer.getTargetAccount() != null ? transfer.getTargetAccount().getAccountNumber() : null,
                transfer.getAmount(),
                transfer.getTransferType(),
                transfer.getStatus(),
                transfer.getCreationDate(),
                "Transferencia creada correctamente"
        );
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDetailResponse> findAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User authenticatedUser) {

        BankAccount account = customerUseCase.findAccount(
                authenticatedUser.getIdentificationNumber(),
                accountNumber
        );

        return ResponseEntity.ok(toAccountDetailResponse(account));
    }

    private static AccountDetailResponse toAccountDetailResponse(BankAccount account) {
        return new AccountDetailResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountStatus(),
                account.getOpeningDate(),
                account.getOwner() != null ? account.getOwner().getIdentificationNumber() : null,
                account.getCatalog() != null ? account.getCatalog().getProductCode() : null,
                account.getCatalog() != null ? account.getCatalog().getProductName() : null,
                account.getCatalog() != null ? account.getCatalog().getDescription() : null
        );
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody CustomerLoanRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        Loan loan = toLoan(request);

        if (authenticatedUser.getCustomer() == null ||
                authenticatedUser.getCustomer().getIdentificationNumber() == null) {
            throw new BusinessException("El usuario no tiene un cliente asociado");
        }

        customerUseCase.createLoan(
                authenticatedUser.getCustomer().getIdentificationNumber(),
                authenticatedUser.getIdentificationNumber(),
                loan
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toLoanResponse(loan));
    }
    
        private static Loan toLoan(CustomerLoanRequest request) {
        Loan loan = new Loan();

        loan.setLoanId(request.getLoanId());
        loan.setLoanType(request.getLoanType());
        loan.setRequestedAmount(request.getRequestedAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());

        BankProductCatalog catalog = new BankProductCatalog();
        catalog.setProductCode(request.getProductCode());

        loan.setCatalog(catalog);

        return loan;
        }

    private static LoanResponse toLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getLoanId(),
                loan.getLoanType(),
                loan.getRequestedAmount(),
                loan.getApprovedAmount(),
                loan.getInterestRate(),
                loan.getTermMonths(),
                loan.getLoanStatus(),
                loan.getCreationDate(),
                loan.getOwner() != null ? loan.getOwner().getIdentificationNumber() : null,
                loan.getCreatedBy() != null ? loan.getCreatedBy().getIdentificationNumber() : null,
                loan.getCatalog() != null ? loan.getCatalog().getProductCode() : null,
                loan.getCatalog() != null ? loan.getCatalog().getProductName() : null
        );
    }
    
    @GetMapping("/loans/{loanId}")
    public ResponseEntity<LoanDetailResponse> findLoan(
            @PathVariable String loanId,
            @AuthenticationPrincipal User authenticatedUser) {

        Loan loan = customerUseCase.findLoan(
                authenticatedUser.getIdentificationNumber(),
                loanId
        );

        return ResponseEntity.ok(toLoanDetailResponse(loan));
    }

    private static LoanDetailResponse toLoanDetailResponse(Loan loan) {
    return new LoanDetailResponse(
            loan.getLoanId(),
            loan.getLoanType(),
            loan.getRequestedAmount(),
            loan.getApprovedAmount(),
            loan.getInterestRate(),
            loan.getTermMonths(),
            loan.getLoanStatus(),
            loan.getCreationDate(),
            loan.getOwner() != null ? loan.getOwner().getIdentificationNumber() : null,
            loan.getCreatedBy() != null ? loan.getCreatedBy().getIdentificationNumber() : null,
            loan.getReviewedBy() != null ? loan.getReviewedBy().getIdentificationNumber() : null,
            loan.getReviewDate(),
            loan.getDisbursementDate(),
            loan.getDisbursementAccount() != null ? loan.getDisbursementAccount().getAccountNumber() : null,
            loan.getCatalog() != null ? loan.getCatalog().getProductCode() : null,
            loan.getCatalog() != null ? loan.getCatalog().getProductName() : null,
            loan.getCatalog() != null ? loan.getCatalog().getDescription() : null
    );
}
}