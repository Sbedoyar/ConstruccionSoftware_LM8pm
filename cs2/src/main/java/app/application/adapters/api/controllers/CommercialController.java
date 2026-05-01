package app.application.adapters.api.controllers;

import app.application.adapters.api.request.AccountRequest;
import app.application.adapters.api.request.BusinessCustomerRequest;
import app.application.adapters.api.request.IndividualCustomerRequest;
import app.application.adapters.api.response.AccountResponse;
import app.application.adapters.api.response.BusinessCustomerResponse;
import app.application.adapters.api.response.CustomerResponse;
import app.application.usecases.CommercialUseCase;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.enums.ProductCategory;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.IndividualCustomer;
import jakarta.validation.Valid;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/commercial")
public class CommercialController {

    @Autowired
    private CommercialUseCase commercialUseCase;

    public CommercialController(CommercialUseCase commercialUseCase) {
        this.commercialUseCase = commercialUseCase;
    }

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @PostMapping("/customers/individual")
    public ResponseEntity<CustomerResponse> createIndividualCustomer(
            @Valid @RequestBody IndividualCustomerRequest request) {

        IndividualCustomer customer = toIndividualCustomer(request);
        commercialUseCase.createIndividualCustomer(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toCustomerResponse(customer));
    }

    @PostMapping("/customers/business")
    public ResponseEntity<BusinessCustomerResponse> createBusinessCustomer(
            @Valid @RequestBody BusinessCustomerRequest request) {

        BusinessCustomer customer = toBusinessCustomer(request);
        commercialUseCase.createBusinessCustomer(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toBusinessCustomerResponse(customer));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        BankAccount account = toBankAccount(request);

        commercialUseCase.createAccount(
                request.getCustomerIdentification(),
                request.getUserIdentification(),
                account
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toAccountResponse(account));
    }

    private static IndividualCustomer toIndividualCustomer(IndividualCustomerRequest request) {
        IndividualCustomer customer = new IndividualCustomer();
        customer.setName(request.getName());
        customer.setIdentificationNumber(request.getIdentificationNumber());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        return customer;
    }

    private static BusinessCustomer toBusinessCustomer(BusinessCustomerRequest request) {
        BusinessCustomer customer = new BusinessCustomer();
        customer.setName(request.getBusinessName());
        customer.setIdentificationNumber(request.getNit());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        IndividualCustomer legalRepresentative = new IndividualCustomer();
        legalRepresentative.setName(request.getLegalRepresentativeName());
        legalRepresentative.setIdentificationNumber(request.getLegalRepresentativeIdentification());

        customer.setLegalRepresentative(legalRepresentative);
        return customer;
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

    private static CustomerResponse toCustomerResponse(IndividualCustomer customer) {
        return new CustomerResponse(
                customer.getName(),
                customer.getIdentificationNumber(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDateOfBirth(),
                customer.getRegistrationDate(),
                customer.getCustomerStatus()
        );
    }

    private static BusinessCustomerResponse toBusinessCustomerResponse(BusinessCustomer customer) {
        return new BusinessCustomerResponse(
                customer.getName(),
                customer.getIdentificationNumber(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate(),
                customer.getCustomerStatus(),
                customer.getLegalRepresentative() != null
                        ? customer.getLegalRepresentative().getName()
                        : null,
                customer.getLegalRepresentative() != null
                        ? customer.getLegalRepresentative().getIdentificationNumber()
                        : null
        );
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
}