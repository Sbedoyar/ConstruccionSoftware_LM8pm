package app.application.adapters.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessCustomerRequest {

    @NotBlank(message = "La razón social es obligatoria")
    private String businessName;

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String email;

    @NotBlank(message = "El número de teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El nombre del representante legal es obligatorio")
    private String legalRepresentativeName;

    @NotBlank(message = "La identificación del representante legal es obligatoria")
    private String legalRepresentativeIdentification;
}