package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectLoanRequest {

    @NotBlank(message = "El ID del préstamo es obligatorio")
    private String loanId;

    @NotBlank(message = "La razón del rechazo es obligatoria")
    private String rejectionReason;
}