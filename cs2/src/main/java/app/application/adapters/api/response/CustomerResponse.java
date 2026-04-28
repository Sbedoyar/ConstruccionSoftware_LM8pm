package app.application.adapters.api.response;

import app.domain.models.enums.CustomerStatus;

import java.time.LocalDate;

public record CustomerResponse(
        String name,
        String identificationNumber,
        String email,
        String phone,
        String address,
        LocalDate dateOfBirth,
        LocalDate registrationDate,
        CustomerStatus customerStatus
) {}