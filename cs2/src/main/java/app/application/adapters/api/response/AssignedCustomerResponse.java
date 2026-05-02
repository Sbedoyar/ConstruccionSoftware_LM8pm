package app.application.adapters.api.response;

import app.domain.models.enums.CustomerStatus;

import java.time.LocalDate;

public record AssignedCustomerResponse(
        String customerType,
        String name,
        String identificationNumber,
        String email,
        String phone,
        String address,
        LocalDate registrationDate,
        CustomerStatus customerStatus,
        LocalDate dateOfBirth,
        String legalRepresentativeName,
        String legalRepresentativeIdentification
) {}
