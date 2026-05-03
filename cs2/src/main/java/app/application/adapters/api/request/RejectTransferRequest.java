package app.application.adapters.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTransferRequest {

    @NotNull(message = "El ID de la transferencia es obligatorio")
    private Integer transferId;
}