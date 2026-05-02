package app.application.adapters.api.request;

import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "La identificación del usuario es obligatoria")
    private String identificationNumber;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    private String name;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol del sistema es obligatorio")
    private RoleType systemRole;

    private UserStatus userStatus;

    private String customerIdentification;
}