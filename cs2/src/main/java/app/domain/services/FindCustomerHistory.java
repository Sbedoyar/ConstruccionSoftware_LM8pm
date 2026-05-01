package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.BankAccount;
import app.domain.models.bankingProduct.BankingProduct;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class FindCustomerHistory {

    private final OperationLogPort operationLogPort;
    private final UserPort userPort;

    //@Autowired
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

        String normalizedProductId = affectedProductId.trim();

        // Se busca el usuario que realiza la consulta.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);

        // RN-22:
        // Solo los clientes pueden consultar su propio historial de operaciones.
        validateCustomerRole(user);

        // RN-22 / RN-23:
        // El cliente solo puede consultar historial de productos propios.
        validateCustomerOwnership(user, normalizedProductId);

        // RN-22:
        // Se consultan los registros de bitácora filtrados por el producto afectado.
        return operationLogPort.findByAffectedProductId(normalizedProductId);
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para realizar esta operación");
        }
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
            .map(this::resolveAffectedProductId)
            .anyMatch(affectedProductId::equals);

        if (!ownsProduct) {
            throw new BusinessException("El cliente no puede consultar historial de productos que no le pertenecen");
        }
    }

    private String resolveAffectedProductId(BankingProduct product) {

        // Este método traduce el producto al identificador que se usa en la bitácora.
        // Para que funcione correctamente, las bitácoras de transferencias se están
        // asociando a la cuenta origen y no al transferId.
        if (product instanceof BankAccount account) {
            return account.getAccountNumber();
        }

        if (product instanceof Loan loan) {
            return loan.getLoanId();
        }

        return null;
    }
}
