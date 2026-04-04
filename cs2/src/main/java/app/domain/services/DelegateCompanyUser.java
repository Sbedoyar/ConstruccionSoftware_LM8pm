package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.CustomerPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DelegateCompanyUser {

    private final UserPort userPort;
    private final CustomerPort customerPort;

    @Autowired
    public DelegateCompanyUser(UserPort userPort, CustomerPort customerPort) {
        this.userPort = userPort;
        this.customerPort = customerPort;
    }

    public void delegateCompanyOperator(String supervisorIdentification,
                                        String targetUserIdentification,
                                        String companyIdentification) throws BusinessException {

        // Validación general:
        // La identificación del supervisor es obligatoria.
        if (supervisorIdentification == null || supervisorIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del supervisor es obligatoria");
        }

        // Validación general:
        // La identificación del usuario objetivo es obligatoria.
        if (targetUserIdentification == null || targetUserIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario objetivo es obligatoria");
        }

        // Validación general:
        // La identificación de la empresa es obligatoria.
        if (companyIdentification == null || companyIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación de la empresa es obligatoria");
        }

        // Se busca el supervisor.
        User supervisor = userPort.findByIdentificationNumber(supervisorIdentification.trim());
        if (supervisor == null) {
            throw new BusinessException("No existe un supervisor con esa identificación");
        }

        // Se busca el usuario que será delegado como operador.
        User targetUser = userPort.findByIdentificationNumber(targetUserIdentification.trim());
        if (targetUser == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Se busca la empresa.
        Customer customer = customerPort.findByIdentificationNumber(companyIdentification.trim());
        if (customer == null) {
            throw new BusinessException("No existe una empresa con esa identificación");
        }

        // RN-AD09:
        // Solo el supervisor de empresa puede gestionar usuarios operativos.
        validateSupervisorRole(supervisor);

        // Validación adicional:
        // El supervisor debe estar activo.
        validateActiveUser(supervisor);

        // Validación adicional:
        // El usuario objetivo también debe estar activo para ser delegado.
        validateActiveUser(targetUser);

        // Validación de tipo:
        // La entidad asociada debe ser una empresa.
        validateBusinessCustomer(customer);

        BusinessCustomer company = (BusinessCustomer) customer;

        // RN-AD09:
        // El supervisor solo puede gestionar usuarios de su propia empresa.
        validateSupervisorCompany(supervisor, company);

        // Validación adicional:
        // No se debe convertir un usuario bancario o analista en operador de empresa.
        validateDelegableTargetUser(targetUser);

        // RN-AD05:
        // El cliente empresa puede delegar permisos a usuarios operativos.
        // Aquí se asigna el usuario a la empresa y se le da rol COMPANY_OPERATOR.
        targetUser.setCustomer(company);
        targetUser.setSystemRole(RoleType.COMPANY_OPERATOR);

        userPort.update(targetUser);
    }

    private void validateSupervisorRole(User supervisor) {

        // RN-AD09:
        // Solo un supervisor de empresa puede delegar usuarios operativos.
        if (supervisor.getSystemRole() != RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("Solo un supervisor de empresa puede delegar usuarios operativos");
        }
    }

    private void validateActiveUser(User user) {

        // Validación adicional:
        // El usuario no puede estar inactivo ni bloqueado.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El usuario debe estar activo para participar en este flujo");
        }
    }

    private void validateBusinessCustomer(Customer customer) {

        // Validación general:
        // La delegación solo aplica para clientes empresa.
        if (!(customer instanceof BusinessCustomer)) {
            throw new BusinessException("La entidad indicada no corresponde a una empresa");
        }
    }

    private void validateSupervisorCompany(User supervisor, BusinessCustomer company) {

        // RN-AD09:
        // El supervisor solo puede gestionar usuarios de la empresa a la que pertenece.
        if (supervisor.getCustomer() == null ||
            !supervisor.getCustomer().getIdentificationNumber().equals(company.getIdentificationNumber())) {
            throw new BusinessException("El supervisor solo puede gestionar usuarios de su empresa");
        }
    }

    private void validateDelegableTargetUser(User targetUser) {

        // Validación adicional:
        // No es correcto reasignar usuarios del banco a un rol operativo de empresa.
        if (targetUser.getSystemRole() == RoleType.INTERNAL_ANALYST ||
            targetUser.getSystemRole() == RoleType.TELLER_EMPLOYEE ||
            targetUser.getSystemRole() == RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessException("No se puede delegar como operador a un usuario del banco");
        }
    }
}
