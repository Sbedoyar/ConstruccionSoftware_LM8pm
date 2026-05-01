package app.application.adapters.api.request;

import app.domain.models.enums.AccountType;
import app.domain.models.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountRequest {

    @NotBlank(message = "La identificación del cliente es obligatoria")
    private String customerIdentification;

    @NotBlank(message = "La identificación del usuario actor es obligatoria")
    private String userIdentification;

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String accountNumber;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType accountType;

    @NotNull(message = "La moneda es obligatoria")
    private CurrencyType currency;

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    private BigDecimal initialBalance;

    @NotBlank(message = "El código del producto es obligatorio")
    private String productCode;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    private String productDescription;

    private Boolean requiresApproval;
}