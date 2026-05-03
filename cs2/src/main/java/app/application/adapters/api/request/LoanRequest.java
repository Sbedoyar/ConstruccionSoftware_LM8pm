package app.application.adapters.api.request;

import app.domain.models.enums.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanRequest {

    @NotBlank(message = "La identificación del cliente es obligatoria")
    private String customerIdentification;

    @NotBlank(message = "El ID del préstamo es obligatorio")
    private String loanId;

    @NotNull(message = "El tipo de préstamo es obligatorio")
    private LoanType loanType;

    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto solicitado debe ser mayor que cero")
    private BigDecimal requestedAmount;

    @NotNull(message = "La tasa de interés es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa de interés debe ser mayor que cero")
    private BigDecimal interestRate;

    @Min(value = 1, message = "El plazo debe ser mayor que cero")
    private int termMonths;

    @NotBlank(message = "El código del producto es obligatorio")
    private String productCode;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    private String productDescription;

    private Boolean requiresApproval;
}