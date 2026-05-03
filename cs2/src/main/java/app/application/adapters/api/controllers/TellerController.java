package app.application.adapters.api.controllers;

import app.application.adapters.api.request.AccountRequest;
import app.application.adapters.api.response.AccountDetailResponse;
import app.application.adapters.api.response.AccountResponse;
import app.application.usecases.TellerUseCase;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.enums.ProductCategory;
import app.domain.models.person.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/teller")
public class TellerController {

    private final TellerUseCase tellerUseCase;

    public TellerController(TellerUseCase tellerUseCase) {
        this.tellerUseCase = tellerUseCase;
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDetailResponse> findAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User authenticatedUser) {

        BankAccount account = tellerUseCase.findAccount(
                authenticatedUser.getIdentificationNumber(),
                accountNumber
        );

        return ResponseEntity.ok(toAccountDetailResponse(account));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        BankAccount account = toBankAccount(request);

        tellerUseCase.createAccount(
                request.getCustomerIdentification(),
                authenticatedUser.getIdentificationNumber(),
                account
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toAccountResponse(account));
    }

    private static BankAccount toBankAccount(AccountRequest request) {
        BankAccount account = new BankAccount();

        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());

        if (request.getInitialBalance() != null) {
            account.setBalance(request.getInitialBalance());
        } else {
            account.setBalance(BigDecimal.ZERO);
        }

        BankProductCatalog catalog = new BankProductCatalog();
        catalog.setProductCode(request.getProductCode());
        catalog.setProductName(request.getProductName());
        catalog.setDescription(request.getProductDescription());
        catalog.setCategory(ProductCategory.ACCOUNT);
        catalog.setRequiresApproval(
                request.getRequiresApproval() != null && request.getRequiresApproval()
        );
        catalog.setActive(true);

        account.setCatalog(catalog);

        return account;
    }

    private static AccountResponse toAccountResponse(BankAccount account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountStatus(),
                account.getOpeningDate(),
                account.getOwner() != null ? account.getOwner().getIdentificationNumber() : null,
                account.getCatalog() != null ? account.getCatalog().getProductCode() : null,
                account.getCatalog() != null ? account.getCatalog().getProductName() : null
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
}