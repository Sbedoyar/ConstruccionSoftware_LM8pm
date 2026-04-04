package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.CustomerPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FindAssignedCustomer {

    private final CustomerPort customerPort;
    private final UserPort userPort;

    @Autowired
    public FindAssignedCustomer(CustomerPort customerPort, UserPort userPort) {
        this.customerPort = customerPort;
        this.userPort = userPort;
    }

    public Customer findAssignedCustomer(String userIdentification, String customerIdentification) throws BusinessException {

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Validación general:
        // La identificación del cliente es obligatoria.
        if (customerIdentification == null || customerIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del cliente es obligatoria");
        }

        // Se busca el empleado comercial.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Validación adicional:
        // El usuario actor debe estar activo.
        validateActiveUser(user);

        // RN-27 / RN-28 / RN-29:
        // Solo el empleado comercial puede consultar clientes asignados en este flujo.
        validateCommercialRole(user);

        // Se busca el cliente.
        Customer customer = customerPort.findByIdentificationNumber(customerIdentification.trim());
        if (customer == null) {
            throw new BusinessException("No existe un cliente con esa identificación");
        }

        // RN-27:
        // El empleado comercial solo puede acceder a clientes asignados o gestionados por él.
        validateAssignedCustomer(user, customer);

        return customer;
    }

    public Loan findCustomerLoanForTracking(String userIdentification, String customerIdentification, String loanId) throws BusinessException {

        // Se reutiliza la validación del cliente asignado.
        Customer customer = findAssignedCustomer(userIdentification, customerIdentification);

        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        if (customer.getBankingProducts() == null) {
            throw new BusinessException("El cliente no tiene productos asociados");
        }

        Loan foundLoan = customer.getBankingProducts().stream()
            .filter(product -> product instanceof Loan)
            .map(product -> (Loan) product)
            .filter(loan -> loanId.trim().equals(loan.getLoanId()))
            .findFirst()
            .orElseThrow(() -> new BusinessException("El cliente no tiene un préstamo con ese ID"));

        // RN-28:
        // El empleado comercial solo puede consultar préstamos en estado IN_REVIEW o REJECTED.
        validateTrackableLoanStatus(foundLoan);

        return foundLoan;
    }

    private void validateActiveUser(User user) {

        // Validación adicional:
        // El usuario actor no puede estar inactivo o bloqueado.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El empleado comercial debe estar activo para consultar clientes asignados");
        }
    }

    private void validateCommercialRole(User user) {

        // RN-27 / RN-28 / RN-29:
        // Solo un empleado comercial puede usar este caso de uso.
        if (user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessException("Solo un empleado comercial puede consultar clientes asignados");
        }
    }

    private void validateAssignedCustomer(User user, Customer customer) {

        // RN-27:
        // El empleado comercial solo puede acceder a clientes asignados o gestionados por él.
        if (user.getAssignedCustomers() == null || user.getAssignedCustomers().stream()
            .filter(assignedCustomer -> assignedCustomer != null)
            .noneMatch(assignedCustomer ->
                assignedCustomer.getIdentificationNumber().equals(customer.getIdentificationNumber()))) {
            throw new BusinessException("El cliente no está asignado al empleado comercial");
        }
    }

    private void validateTrackableLoanStatus(Loan loan) {

        // RN-28:
        // El empleado comercial puede consultar préstamos en estado IN_REVIEW o REJECTED,
        // pero no puede modificar su estado.
        if (loan.getLoanStatus() != LoanStatus.IN_REVIEW &&
            loan.getLoanStatus() != LoanStatus.REJECTED) {
            throw new BusinessException("El empleado comercial solo puede consultar préstamos en estudio o rechazados");
        }
    }
}
