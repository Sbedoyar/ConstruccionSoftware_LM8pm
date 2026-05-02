package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTransferRequest {

    @NotNull(message = "El ID de la transferencia es obligatorio")
    private Integer transferId;

    @NotBlank(message = "La identificación del supervisor es obligatoria")
    private String supervisorIdentification;
}