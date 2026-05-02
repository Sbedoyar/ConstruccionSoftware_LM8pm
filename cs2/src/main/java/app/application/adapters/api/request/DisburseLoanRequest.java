package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisburseLoanRequest {

    @NotBlank(message = "El ID del préstamo es obligatorio")
    private String loanId;

    @NotBlank(message = "La identificación del analista es obligatoria")
    private String analystIdentification;

    @NotBlank(message = "El número de cuenta destino es obligatorio")
    private String disbursementAccountNumber;
}