package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.RoleType;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.OperationLogPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindCustomerHistory {

    private final OperationLogPort operationLogPort;
    private final UserPort userPort;

    @Autowired
    public FindCustomerHistory(OperationLogPort operationLogPort, UserPort userPort) {
        this.operationLogPort = operationLogPort;
        this.userPort = userPort;
    }

    public List<OperationLog> findHistoryByProduct(String userIdentification, String affectedProductId) throws BusinessException {

        // Validación general:
        // La identificación del usuario es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Validación general:
        // El ID del producto afectado es obligatorio.
        if (affectedProductId == null || affectedProductId.trim().isEmpty()) {
            throw new BusinessException("El ID del producto afectado es obligatorio");
        }

        // Se busca el usuario que realiza la consulta.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // RN-22:
        // Solo los clientes pueden consultar su propio historial de operaciones.
        validateCustomerRole(user);

        // RN-22 / RN-23:
        // El cliente solo puede consultar historial de productos propios.
        validateCustomerOwnership(user, affectedProductId);

        // RN-22:
        // Se consultan los registros de bitácora filtrados por el producto afectado.
        return operationLogPort.findByAffectedProductId(affectedProductId.trim());
    }

    private void validateCustomerRole(User user) {

        // RN-22:
        // Solo un cliente persona natural o cliente empresa puede consultar
        // historial propio en la bitácora.
        if (user.getSystemRole() != RoleType.INDIVIDUAL_CUSTOMER &&
            user.getSystemRole() != RoleType.BUSINESS_CUSTOMER) {
            throw new BusinessException("Solo un cliente puede consultar su historial de operaciones");
        }
    }

    private void validateCustomerOwnership(User user, String affectedProductId) {

        // RN-22 / RN-23:
        // El cliente solo puede consultar historial de productos propios.
        if (user.getCustomer() == null || user.getCustomer().getBankingProducts() == null) {
            throw new BusinessException("El cliente no tiene productos asociados");
        }

        boolean ownsProduct = user.getCustomer().getBankingProducts().stream()
            .anyMatch(product -> product.getCatalog() != null &&
                                 affectedProductId.trim().equals(resolveAffectedProductId(product)));

        if (!ownsProduct) {
            throw new BusinessException("El cliente no puede consultar historial de productos que no le pertenecen");
        }
    }

    private String resolveAffectedProductId(app.domain.models.bankingProduct.BankingProduct product) {

        // Este método traduce el producto al identificador que se usa en la bitácora.
        // Debe ajustarse según cómo estén guardando los IDs de cuentas y préstamos.
        if (product instanceof app.domain.models.bankingProduct.BankAccount account) {
            return account.getAccountNumber();
        }

        if (product instanceof app.domain.models.bankingProduct.Loan loan) {
            return loan.getLoanId();
        }

        return null;
    }
}