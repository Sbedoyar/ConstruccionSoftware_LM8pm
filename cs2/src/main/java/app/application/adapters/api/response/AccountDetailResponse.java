package app.application.adapters.api.response;

import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountDetailResponse(
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        CurrencyType currency,
        AccountStatus accountStatus,
        LocalDate openingDate,
        String ownerIdentification,
        String productCode,
        String productName,
        String productDescription
) {}