package app.application.adapters.api.controllers;

import app.application.adapters.api.request.AccountRequest;
import app.application.adapters.api.request.BusinessCustomerRequest;
import app.application.adapters.api.request.IndividualCustomerRequest;
import app.application.adapters.api.request.LoanRequest;
import app.application.adapters.api.response.AccountResponse;
import app.application.adapters.api.response.AssignedCustomerResponse;
import app.application.adapters.api.response.BusinessCustomerResponse;
import app.application.adapters.api.response.CustomerResponse;
import app.application.adapters.api.response.LoanResponse;
import app.application.usecases.CommercialUseCase;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankProductCatalog;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.ProductCategory;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        BankAccount account = toBankAccount(request);

        commercialUseCase.createAccount(
                request.getCustomerIdentification(),
                authenticatedUser.getIdentificationNumber(),
                account
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toAccountResponse(account));
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody LoanRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        Loan loan = toLoan(request);

        commercialUseCase.createLoan(
                request.getCustomerIdentification(),
                authenticatedUser.getIdentificationNumber(),
                loan
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toLoanResponse(loan));
    }

    @GetMapping("/customers/assigned/{customerIdentification}")
    public ResponseEntity<AssignedCustomerResponse> findAssignedCustomer(
            @PathVariable String customerIdentification,
            @AuthenticationPrincipal User authenticatedUser) {

        Customer customer = commercialUseCase.findAssignedCustomer(
                authenticatedUser.getIdentificationNumber(),
                customerIdentification
        );

        return ResponseEntity.ok(toAssignedCustomerResponse(customer));
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

        account.setCatalog(catalog);

        return account;
    }

    private static Loan toLoan(LoanRequest request) {
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
    private static AssignedCustomerResponse toAssignedCustomerResponse(Customer customer) {
        String customerType = "INDIVIDUAL";
        LocalDate dateOfBirth = null;
        String legalRepresentativeName = null;
        String legalRepresentativeIdentification = null;

        if (customer instanceof IndividualCustomer individualCustomer) {
            dateOfBirth = individualCustomer.getDateOfBirth();
        }

        if (customer instanceof BusinessCustomer businessCustomer) {
            customerType = "BUSINESS";

            if (businessCustomer.getLegalRepresentative() != null) {
                legalRepresentativeName = businessCustomer.getLegalRepresentative().getName();
                legalRepresentativeIdentification = businessCustomer.getLegalRepresentative().getIdentificationNumber();
            }
        }

        return new AssignedCustomerResponse(
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
}