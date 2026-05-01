package app.application.adapters.api.response;

import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;

public record UserResponse(
        Long id,
        String identificationNumber,
        String name,
        String email,
        String phone,
        String address,
        String username,
        RoleType systemRole,
        UserStatus userStatus
) {}