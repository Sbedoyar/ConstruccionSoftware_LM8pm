package app.application.adapters.api.response;

import app.domain.models.enums.CustomerStatus;

import java.time.LocalDate;

public record BusinessCustomerResponse(
        String businessName,
        String nit,
        String email,
        String phone,
        String address,
        LocalDate registrationDate,
        CustomerStatus customerStatus,
        String legalRepresentativeName,
        String legalRepresentativeIdentification
) {}