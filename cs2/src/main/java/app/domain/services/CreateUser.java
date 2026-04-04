package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateUser {

    private final UserPort userPort;

    @Autowired
    public CreateUser(UserPort userPort) {
        this.userPort = userPort;
    }

    public void createUser(User user) throws BusinessException {

        // Validación general:
        // El usuario es obligatorio.
        if (user == null) {
            throw new BusinessException("El usuario es obligatorio");
        }

        // Validación general:
        // El nombre es obligatorio.
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre del usuario es obligatorio");
        }

        // Validación general:
        // El username es obligatorio.
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es obligatorio");
        }

        // Validación general:
        // La contraseña es obligatoria.
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria");
        }

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (user.getIdentificationNumber() == null || user.getIdentificationNumber().trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Validación general:
        // El correo es obligatorio y debe tener formato válido.
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessException("El correo del usuario es obligatorio");
        }

        if (!user.getEmail().contains("@") || user.getEmail().lastIndexOf('.') < user.getEmail().indexOf('@')) {
            throw new BusinessException("El correo del usuario no tiene un formato válido");
        }

        // Validación general:
        // El teléfono es obligatorio.
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new BusinessException("El teléfono del usuario es obligatorio");
        }

        // Validación general:
        // El teléfono debe tener entre 7 y 15 caracteres.
        if (user.getPhone().length() < 7 || user.getPhone().length() > 15) {
            throw new BusinessException("El teléfono debe tener entre 7 y 15 caracteres");
        }

        // Validación general:
        // La dirección es obligatoria.
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new BusinessException("La dirección del usuario es obligatoria");
        }

        // Validación general:
        // El rol del usuario es obligatorio.
        if (user.getSystemRole() == null) {
            throw new BusinessException("El rol del usuario es obligatorio");
        }

        // Validación general:
        // El estado del usuario es obligatorio.
        if (user.getUserStatus() == null) {
            throw new BusinessException("El estado del usuario es obligatorio");
        }

        // Regla general:
        // La identificación del usuario debe ser única.
        if (userPort.findByIdentificationNumber(user.getIdentificationNumber()) != null) {
            throw new BusinessException("Ya existe un usuario con esa identificación");
        }

        // Regla de relación:
        // Según el rol, se valida si debe o no tener customer asociado.
        validateCustomerRequirement(user);

        // Regla de consistencia:
        // El tipo de customer debe coincidir con el rol del usuario.
        validateCustomerTypeByRole(user);

        // Regla de asignación:
        // Solo el empleado comercial puede tener clientes asignados.
        validateAssignedCustomers(user);

        userPort.save(user);

    }

    private void validateCustomerRequirement(User user) {

        switch (user.getSystemRole()) {

            // Estos roles representan a un cliente o empresa cliente,
            // por lo tanto deben tener customer asociado.
            case INDIVIDUAL_CUSTOMER:
            case BUSINESS_CUSTOMER:
            case COMPANY_OPERATOR:
            case COMPANY_SUPERVISOR:
                if (user.getCustomer() == null) {
                    throw new BusinessException("Este rol requiere un cliente asociado");
                }
                break;

            // Estos roles son internos del banco,
            // por lo tanto no deben tener customer asociado.
            case TELLER_EMPLOYEE:
            case COMMERCIAL_EMPLOYEE:
            case INTERNAL_ANALYST:
                if (user.getCustomer() != null) {
                    throw new BusinessException("Este rol no debe tener un cliente asociado");
                }
                break;

            default:
                throw new BusinessException("Rol de usuario no válido");
        }
    }

    private void validateCustomerTypeByRole(User user) {

        if (user.getCustomer() == null) {
            return;
        }

        switch (user.getSystemRole()) {

            // El cliente individual debe estar ligado a un IndividualCustomer.
            case INDIVIDUAL_CUSTOMER:
                if (!(user.getCustomer() instanceof IndividualCustomer)) {
                    throw new BusinessException("El rol INDIVIDUAL_CUSTOMER debe asociarse a un cliente individual");
                }
                break;

            // Los roles empresariales deben estar ligados a una empresa.
            case BUSINESS_CUSTOMER:
            case COMPANY_OPERATOR:
            case COMPANY_SUPERVISOR:
                if (!(user.getCustomer() instanceof BusinessCustomer)) {
                    throw new BusinessException("Este rol debe asociarse a un cliente empresa");
                }
                break;

            default:
                break;
        }
    }

    private void validateAssignedCustomers(User user) {

        // Solo el empleado comercial puede tener clientes asignados.
        if (user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE
                && user.getAssignedCustomers() != null
                && !user.getAssignedCustomers().isEmpty()) {
            throw new BusinessException("Solo un empleado comercial puede tener clientes asignados");
        }
    }
}