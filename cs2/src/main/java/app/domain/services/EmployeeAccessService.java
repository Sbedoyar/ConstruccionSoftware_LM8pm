package app.domain.services;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.enums.RoleType;
import app.domain.models.person.Customer;
import app.domain.models.person.User;

public class EmployeeAccessService {

    //TELLER_EMPLOYEE.

    // ==============================
    // RN24: Puede consultar el Saldo_Actual y 
    // el Estado_Cuenta de cualquier cliente para propósitos de transacciones de caja.
    // ==============================
    public void validateCashierAccess(User user) {

        if (user.getSystemRole() != RoleType.TELLER_EMPLOYEE) {
            throw new BusinessExceptions("Acceso denegado: solo ventanilla");
        }
    }

    // ==============================
    // RN25: Puede registrar la apertura de nuevos productos de cuenta.
    // ==============================
    public void validateAccountCreation(User user) {

        if (user.getSystemRole() != RoleType.TELLER_EMPLOYEE) {
            throw new BusinessExceptions("No tiene permisos para crear cuentas");
        }
    }

    // ==============================
    // RN26: No puede acceder a información de análisis de riesgo, 
    // datos crediticios detallados (como la Tasa de Interés) 
    // o la Bitácora completa de operaciones.
    // ==============================
    public void validateRestrictedAccess(User user) {

        if (user.getSystemRole() == RoleType.TELLER_EMPLOYEE) {
            throw new BusinessExceptions(
                "Acceso restringido: no puede ver datos de riesgo, tasas o bitácora completa"
            );
        }
    }

    //COMMERCIAL_EMPLOYEE

    // RN27: Puede acceder a la información completa de los clientes 
    // que tiene asignados (o que está gestionando para una solicitud).

    public void validateCommercialCustomerAccess(User user, Customer customer) {

        if (user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessExceptions("Acceso solo para empleados comerciales");
        }

        // IMPORTANTE: validar que el cliente esté asignado
        if (!user.getAssignedCustomers().contains(customer)) {
            throw new BusinessExceptions("No tiene acceso a este cliente");
        }
    }

    // RN28: Puede consultar el estado de los préstamos en 
    // "En estudio" o "Rechazado" para dar seguimiento, 
    // pero no puede modificar su estado.

    public void validateLoanView(User user) {

        if (user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessExceptions("Acceso denegado");
        }
    }

    public void validateLoanModification(User user) {

        if (user.getSystemRole() == RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessExceptions("No puede modificar el estado del préstamo");
        }
    }

    // RN29: No puede realizar operaciones que impacten saldos directamente, 
    // salvo la solicitud inicial de productos.

    public void validateNoBalanceOperations(User user) {
        if (user == null) {
            throw new BusinessExceptions("Usuario inválido");
        }

        if (user.getSystemRole() == RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessExceptions("No puede realizar operaciones que afecten saldos");
        }
    }

    //COMPANY_OPERATOR
}
