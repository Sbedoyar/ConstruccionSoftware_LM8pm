package app.application.adapters.api.response;

import app.domain.models.enums.RoleType;

public record LoginResponse(
        String token,
        String tokenType,
        String identificationNumber,
        String username,
        RoleType role
) {}