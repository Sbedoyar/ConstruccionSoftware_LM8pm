package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelegateCompanyOperatorRequest {

    @NotBlank(message = "La identificación del delegador es obligatoria")
    private String delegatorIdentification;

    @NotBlank(message = "La identificación del usuario objetivo es obligatoria")
    private String targetUserIdentification;

    @NotBlank(message = "La identificación de la empresa es obligatoria")
    private String companyIdentification;
}