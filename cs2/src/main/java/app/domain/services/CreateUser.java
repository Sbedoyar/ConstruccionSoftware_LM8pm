package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import app.domain.ports.out.CustomerPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUser {

    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;
    private final CustomerPort customerPort;

    @Autowired
    public CreateUser(UserPort userPort, PasswordEncoder passwordEncoder, CustomerPort customerPort) {
        this.userPort = userPort;
        this.passwordEncoder = passwordEncoder;
        this.customerPort = customerPort;
    }

    public void createUser(User user) throws BusinessException {

        if (user == null) {
            throw new BusinessException("El usuario es obligatorio");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre del usuario es obligatorio");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es obligatorio");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria");
        }

        if (user.getIdentificationNumber() == null || user.getIdentificationNumber().trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessException("El correo del usuario es obligatorio");
        }

        if (!user.getEmail().contains("@") || user.getEmail().lastIndexOf('.') < user.getEmail().indexOf('@')) {
            throw new BusinessException("El correo del usuario no tiene un formato válido");
        }

        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new BusinessException("El teléfono del usuario es obligatorio");
        }

        if (user.getPhone().trim().length() < 7 || user.getPhone().trim().length() > 15) {
            throw new BusinessException("El teléfono debe tener entre 7 y 15 caracteres");
        }

        if (!user.getPhone().trim().matches("\\d+")) {
            throw new BusinessException("El teléfono solo debe contener dígitos");
        }

        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new BusinessException("La dirección del usuario es obligatoria");
        }

        if (user.getSystemRole() == null) {
            throw new BusinessException("El rol del usuario es obligatorio");
        }

        if (user.getUserStatus() == null) {
            throw new BusinessException("El estado del usuario es obligatorio");
        }

        if (userPort.findByIdentificationNumber(user.getIdentificationNumber().trim()) != null) {
            throw new BusinessException("Ya existe un usuario con esa identificación");
        }

        if (userPort.findByUsername(user.getUsername().trim()) != null) {
            throw new BusinessException("Ya existe un usuario con ese nombre de usuario");
        }

        resolveCustomerAssociation(user);
        validateCustomerRequirement(user);
        validateCustomerTypeByRole(user);
        validateAssignedCustomers(user);

        user.setName(user.getName().trim());
        user.setUsername(user.getUsername().trim());
        user.setIdentificationNumber(user.getIdentificationNumber().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhone(user.getPhone().trim());
        user.setAddress(user.getAddress().trim());

        user.setPassword(passwordEncoder.encode(user.getPassword().trim()));

        userPort.save(user);
    }

    private void validateCustomerRequirement(User user) {

        if (user.getSystemRole() == RoleType.COMPANY_OPERATOR) {
            throw new BusinessException("El rol COMPANY_OPERATOR no se puede asignar directamente. Primero cree el usuario como PENDING_COMPANY_OPERATOR y luego delegue el permiso.");
        }

        switch (user.getSystemRole()) {

            case INDIVIDUAL_CUSTOMER:
            case BUSINESS_CUSTOMER:
            case COMPANY_SUPERVISOR:
            case PENDING_COMPANY_OPERATOR:
                if (user.getCustomer() == null) {
                    throw new BusinessException("Este rol requiere un cliente asociado");
                }
                break;

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

            case INDIVIDUAL_CUSTOMER:
                if (!(user.getCustomer() instanceof IndividualCustomer)) {
                    throw new BusinessException("El rol INDIVIDUAL_CUSTOMER debe asociarse a un cliente individual");
                }

                if (user.getCustomer().getIdentificationNumber() == null ||
                        !user.getIdentificationNumber().trim()
                                .equals(user.getCustomer().getIdentificationNumber().trim())) {
                    throw new BusinessException("El usuario cliente individual debe tener la misma identificación que su cliente asociado");
                }
                break;

            case BUSINESS_CUSTOMER:
            case COMPANY_SUPERVISOR:
            case PENDING_COMPANY_OPERATOR:
                if (!(user.getCustomer() instanceof BusinessCustomer)) {
                    throw new BusinessException("Este rol debe asociarse a un cliente empresa");
                }
                break;

            case COMPANY_OPERATOR:
                throw new BusinessException("El rol COMPANY_OPERATOR solo puede asignarse mediante delegación");

            default:
                break;
        }
    }

    private void validateAssignedCustomers(User user) {

        if (user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE
                && user.getAssignedCustomers() != null
                && !user.getAssignedCustomers().isEmpty()) {
            throw new BusinessException("Solo un empleado comercial puede tener clientes asignados");
        }
    }

    private void resolveCustomerAssociation(User user) {

        if (user.getCustomer() == null) {
            return;
        }

        if (user.getCustomer().getIdentificationNumber() == null ||
                user.getCustomer().getIdentificationNumber().trim().isEmpty()) {
            user.setCustomer(null);
            return;
        }

        String customerIdentification = user.getCustomer().getIdentificationNumber().trim();

        Customer existingCustomer = customerPort.findByIdentificationNumber(customerIdentification);

        if (existingCustomer == null) {
            throw new BusinessException("No existe un cliente con la identificación asociada");
        }

        user.setCustomer(existingCustomer);
    }
}