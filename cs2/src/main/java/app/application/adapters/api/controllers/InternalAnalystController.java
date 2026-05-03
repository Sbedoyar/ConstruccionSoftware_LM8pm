package app.application.adapters.api.controllers;

import app.application.adapters.api.request.ApproveLoanRequest;
import app.application.adapters.api.request.DisburseLoanRequest;
import app.application.adapters.api.request.RejectLoanRequest;
import app.application.adapters.api.response.AccountDetailResponse;
import app.application.adapters.api.response.ApproveLoanResponse;
import app.application.adapters.api.response.CustomerDetailResponse;
import app.application.adapters.api.response.DisburseLoanResponse;
import app.application.adapters.api.response.LoanDetailResponse;
import app.application.adapters.api.response.OperationLogResponse;
import app.application.adapters.api.response.RejectLoanResponse;
import app.application.adapters.api.response.TransferDetailResponse;
import app.application.usecases.InternalAnalystUseCase;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import app.domain.models.transfer.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @Valid @RequestBody ApproveLoanRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        internalAnalystUseCase.approveLoan(
                request.getLoanId(),
                authenticatedUser.getIdentificationNumber(),
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
            @Valid @RequestBody RejectLoanRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        internalAnalystUseCase.rejectLoan(
                request.getLoanId(),
                authenticatedUser.getIdentificationNumber(),
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
            @Valid @RequestBody DisburseLoanRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        internalAnalystUseCase.disburseLoan(
                request.getLoanId(),
                authenticatedUser.getIdentificationNumber(),
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
            @AuthenticationPrincipal User authenticatedUser) {

        List<OperationLog> logs = internalAnalystUseCase.findAllLogs(
                authenticatedUser.getIdentificationNumber()
        );

        List<OperationLogResponse> response = logs.stream()
                .map(InternalAnalystController::toOperationLogResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerIdentification}")
    public ResponseEntity<CustomerDetailResponse> findCustomer(
            @PathVariable String customerIdentification,
            @AuthenticationPrincipal User authenticatedUser) {

        Customer customer = internalAnalystUseCase.findCustomer(
                authenticatedUser.getIdentificationNumber(),
                customerIdentification
        );

        return ResponseEntity.ok(toCustomerDetailResponse(customer));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDetailResponse> findAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User authenticatedUser) {

        BankAccount account = internalAnalystUseCase.findAccount(
                authenticatedUser.getIdentificationNumber(),
                accountNumber
        );

        return ResponseEntity.ok(toAccountDetailResponse(account));
    }

    @GetMapping("/loans/{loanId}")
    public ResponseEntity<LoanDetailResponse> findLoan(
            @PathVariable String loanId,
            @AuthenticationPrincipal User authenticatedUser) {

        Loan loan = internalAnalystUseCase.findLoan(
                authenticatedUser.getIdentificationNumber(),
                loanId
        );

        return ResponseEntity.ok(toLoanDetailResponse(loan));
    }

    @GetMapping("/transfers/{transferId}")
    public ResponseEntity<TransferDetailResponse> findTransfer(
            @PathVariable int transferId,
            @AuthenticationPrincipal User authenticatedUser) {

        Transfer transfer = internalAnalystUseCase.findTransfer(
                authenticatedUser.getIdentificationNumber(),
                transferId
        );

        return ResponseEntity.ok(toTransferDetailResponse(transfer));
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

    private static CustomerDetailResponse toCustomerDetailResponse(Customer customer) {
        String customerType = "CUSTOMER";
        LocalDate dateOfBirth = null;
        String legalRepresentativeName = null;
        String legalRepresentativeIdentification = null;

        if (customer instanceof IndividualCustomer individualCustomer) {
            customerType = "INDIVIDUAL";
            dateOfBirth = individualCustomer.getDateOfBirth();
        }

        if (customer instanceof BusinessCustomer businessCustomer) {
            customerType = "BUSINESS";

            if (businessCustomer.getLegalRepresentative() != null) {
                legalRepresentativeName = businessCustomer.getLegalRepresentative().getName();
                legalRepresentativeIdentification = businessCustomer.getLegalRepresentative().getIdentificationNumber();
            }
        }

        return new CustomerDetailResponse(
                customerType,
                customer.getName(),
                customer.getIdentificationNumber(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate(),
                customer.getCustomerStatus(),
                dateOfBirth,
                legalRepresentativeName,
                legalRepresentativeIdentification
        );
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