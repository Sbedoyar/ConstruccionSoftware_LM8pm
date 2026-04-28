package app.application.adapters.api.controllers;

import app.application.adapters.api.request.IndividualCustomerRequest;
import app.application.adapters.api.response.CustomerResponse;
import app.application.usecases.CommercialUseCase;
import app.domain.models.person.IndividualCustomer;
import jakarta.validation.Valid;
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
}