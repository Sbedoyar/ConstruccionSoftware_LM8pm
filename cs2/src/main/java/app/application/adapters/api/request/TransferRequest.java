package app.application.adapters.api.request;

import app.domain.models.enums.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotBlank(message = "La cuenta origen es obligatoria")
    private String sourceAccountNumber;

    private String targetAccountNumber;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal amount;

    @NotNull(message = "El tipo de transferencia es obligatorio")
    private TransferType transferType;
}