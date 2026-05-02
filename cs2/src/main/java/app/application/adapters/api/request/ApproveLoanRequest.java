package app.application.adapters.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApproveLoanRequest {

    @NotBlank(message = "El ID del préstamo es obligatorio")
    private String loanId;

    @NotBlank(message = "La identificación del analista es obligatoria")
    private String analystIdentification;

    @NotNull(message = "El monto aprobado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto aprobado debe ser mayor que cero")
    private BigDecimal approvedAmount;
}