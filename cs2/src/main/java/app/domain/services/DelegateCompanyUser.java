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

//@Service
public class DelegateCompanyUser {

    private final UserPort userPort;
    private final CustomerPort customerPort;

    //@Autowired
    public DelegateCompanyUser(UserPort userPort, CustomerPort customerPort) {
        this.userPort = userPort;
        this.customerPort = customerPort;
    }

    public void delegateCompanyOperator(String delegatorIdentification,
                                        String targetUserIdentification,
                                        String companyIdentification) throws BusinessException {

        // Validación general:
        // La identificación del usuario delegador es obligatoria.
        if (delegatorIdentification == null || delegatorIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario delegador es obligatoria");
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

        // Se busca el usuario delegador.
        User delegator = userPort.findByIdentificationNumber(delegatorIdentification.trim());
        if (delegator == null) {
            throw new BusinessException("No existe un usuario delegador con esa identificación");
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

        // RN-AD05 / RN-AD09:
        // La delegación de usuarios operativos puede ser realizada por:
        // - El cliente empresa administrador
        // - El supervisor de empresa
        validateDelegatorRole(delegator);

        // Validación adicional:
        // El usuario delegador debe estar activo.
        validateActiveUser(delegator);

        // Validación adicional:
        // El usuario objetivo también debe estar activo para ser delegado.
        validateActiveUser(targetUser);

        // Validación de tipo:
        // La entidad asociada debe ser una empresa.
        validateBusinessCustomer(customer);

        BusinessCustomer company = (BusinessCustomer) customer;

        // RN-AD05 / RN-AD09:
        // El delegador solo puede gestionar usuarios de la empresa a la que pertenece.
        validateDelegatorCompany(delegator, company);

        // Validación adicional:
        // El usuario objetivo debe poder convertirse en operador de empresa.
        validateDelegableTargetUser(targetUser, company);

        // RN-AD05:
        // El cliente empresa puede delegar permisos a usuarios operativos.
        // Aquí se asigna el usuario a la empresa y se le da rol COMPANY_OPERATOR.
        targetUser.setCustomer(company);
        targetUser.setSystemRole(RoleType.COMPANY_OPERATOR);

        userPort.update(targetUser);
    }

    private void validateDelegatorRole(User delegator) {

        // RN-AD05 / RN-AD09:
        // La delegación de usuarios operativos puede ser realizada por:
        // - El cliente empresa administrador
        // - El supervisor de empresa
        if (delegator.getSystemRole() != RoleType.BUSINESS_CUSTOMER &&
            delegator.getSystemRole() != RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("Solo el administrador o supervisor de empresa puede delegar usuarios operativos");
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

    private void validateDelegatorCompany(User delegator, BusinessCustomer company) {

        // RN-AD05 / RN-AD09:
        // El delegador solo puede gestionar usuarios de la empresa a la que pertenece.
        if (delegator.getCustomer() == null ||
            !delegator.getCustomer().getIdentificationNumber().equals(company.getIdentificationNumber())) {
            throw new BusinessException("El usuario solo puede gestionar usuarios de su empresa");
        }
    }

    private void validateDelegableTargetUser(User targetUser, BusinessCustomer company) {

        // Validación adicional:
        // No se puede delegar a usuarios internos del banco.
        if (targetUser.getSystemRole() == RoleType.INTERNAL_ANALYST ||
            targetUser.getSystemRole() == RoleType.TELLER_EMPLOYEE ||
            targetUser.getSystemRole() == RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessException("No se puede delegar como operador a un usuario del banco");
        }

        // Validación adicional:
        // Un cliente individual no puede convertirse en operador de empresa.
        if (targetUser.getSystemRole() == RoleType.INDIVIDUAL_CUSTOMER) {
            throw new BusinessException("Un cliente individual no puede ser delegado como operador de empresa");
        }

        // Validación adicional:
        // Un supervisor de empresa no debe degradarse a operador.
        if (targetUser.getSystemRole() == RoleType.COMPANY_SUPERVISOR) {
            throw new BusinessException("No se puede delegar como operador a un supervisor de empresa");
        }

        // Validación adicional:
        // Un administrador de empresa no debe degradarse a operador.
        if (targetUser.getSystemRole() == RoleType.BUSINESS_CUSTOMER) {
            throw new BusinessException("No se puede delegar como operador a un administrador de empresa");
        }

        // Validación adicional:
        // Si el usuario ya está asociado a otra empresa, no puede reasignarse aquí.
        if (targetUser.getCustomer() != null
                && !targetUser.getCustomer().getIdentificationNumber().equals(company.getIdentificationNumber())) {
            throw new BusinessException("El usuario ya está asociado a otra empresa");
        }
    }
}