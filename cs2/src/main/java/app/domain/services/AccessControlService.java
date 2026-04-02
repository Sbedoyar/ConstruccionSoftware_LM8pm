package app.domain.services;

import java.util.List;
import java.util.stream.Collectors;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.OperationLogPort;

public class AccessControlService {
    private final OperationLogPort logPort;
    private final EmployeeAccessService employeeAccessService;

    public AccessControlService(OperationLogPort logPort, EmployeeAccessService employeeAccessService) {
        this.logPort = logPort;
        this.employeeAccessService = employeeAccessService;
    }

    // ==============================
    // RN21: Solo pueden consultar y operar 
    // sobre sus propios productos (Cuenta Bancaria, Préstamo / Crédito).

    // RN23: No pueden ver ninguna información o producto asociado a otros clientes.
    // ==============================
    public void validateUserAccessToAccount(User user, BankAccount account) {

        // Usuario activo
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessExceptions("Usuario inactivo");
        }

        // Cliente
        if (user.getCustomer() != null) {
            if (!account.getOwner().getId().equals(user.getCustomer().getId())) {
                throw new BusinessExceptions("Acceso denegado");
            }
            return;
        }

        // Empleado banco
        if (user.getAssignedCustomers() != null) {
            boolean hasAccess = user.getAssignedCustomers().stream()
                .anyMatch(c -> c.getId().equals(account.getOwner().getId()));

            if (!hasAccess) {
                throw new BusinessExceptions("Acceso denegado");
            }
            return;
        }

        // Empresa
        if (user.getCompany() != null) {
            if (!account.getOwner().getId().equals(user.getCompany().getId())) {
                throw new BusinessExceptions("Acceso denegado");
            }
            return;
        }

        throw new BusinessExceptions("Usuario sin permisos");
    }

    // ==============================
    // RN22: Pueden ver el historial de sus propias operaciones 
    // registradas en la Bitácora, filtrado por su ID_Producto_Afectado.
    // ==============================
    public List<OperationLog> getUserLogs(User user, String productId) {

        // RN26: restringir acceso
        employeeAccessService.validateRestrictedAccess(user);

        List<OperationLog> logs = logPort.findByProductId(productId);

        return logs.stream()
                .filter(log -> 
                    log.getUser().getCustomer().getId()
                        .equals(user.getCustomer().getId())
                )
                .collect(Collectors.toList());
    }

}
