package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.ProductCategory;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.out.CustomerPort;
import app.domain.ports.out.LoanPort;
import app.domain.ports.out.OperationLogPort;
import app.domain.ports.out.UserPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Service
public class CreateLoan {

    private final LoanPort loanPort;
    private final CustomerPort customerPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    //@Autowired
    public CreateLoan(LoanPort loanPort,
                      CustomerPort customerPort,
                      UserPort userPort,
                      OperationLogPort operationLogPort) {
        this.loanPort = loanPort;
        this.customerPort = customerPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    public void createLoan(String customerIdentification, String userIdentification, Loan loan) throws BusinessException {

        // Validación general de entrada.
        // No corresponde a una RN numerada, pero evita procesar un préstamo nulo.
        if (loan == null) {
            throw new BusinessException("El préstamo no puede ser null");
        }

        // Validación general:
        // La identificación del cliente solicitante es obligatoria.
        if (customerIdentification == null || customerIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del cliente es obligatoria");
        }

        // Validación general:
        // La identificación del usuario creador es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca el cliente solicitante.
        Customer customer = customerPort.findByIdentificationNumber(customerIdentification.trim());
        if (customer == null) {
            throw new BusinessException("No existe un cliente con esa identificación");
        }

        // Se busca el usuario que crea la solicitud.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // Validación general:
        // El usuario actor debe estar activo.
        validateActiveUser(user);

        // RN-06:
        // Todo préstamo debe estar asociado a un cliente válido y activo.
        validateActiveCustomer(customer);

        // RN-AD11:
        // La solicitud de préstamo puede ser creada por:
        // - Cliente persona natural
        // - Cliente empresa
        // - Empleado comercial
        validateAuthorizedRole(user);

        // RN-AD11 / RN-27:
        // Además del rol, se valida que el usuario pueda crear el préstamo
        // para ese cliente específico.
        validateActorAccessToCustomer(user, customer);

        // Regla general del préstamo:
        // El identificador del préstamo es obligatorio.
        validateLoanId(loan.getLoanId());

        // Regla general del préstamo:
        // El identificador del préstamo debe ser único.
        validateUniqueLoanId(loan.getLoanId().trim());

        // Normalización del ID del préstamo.
        loan.setLoanId(loan.getLoanId().trim());

        // Validación general:
        // El tipo de préstamo es obligatorio.
        validateLoanType(loan);

        // Regla general del préstamo:
        // El monto solicitado debe ser mayor que cero.
        validateRequestedAmount(loan.getRequestedAmount());

        // Regla general del préstamo:
        // La tasa de interés debe ser mayor que cero.
        validateInterestRate(loan.getInterestRate());

        // Regla general del préstamo:
        // El plazo en meses debe ser mayor que cero.
        validateTermMonths(loan.getTermMonths());

        // Regla general del préstamo:
        // El préstamo debe estar asociado a un producto válido del catálogo.
        validateLoanCatalog(loan);

        // RN-AD12:
        // Toda solicitud de préstamo se registra inicialmente con estado "En estudio".
        loan.setLoanStatus(LoanStatus.IN_REVIEW);

        // Se asignan los datos del flujo de creación.
        loan.setOwner(customer);
        loan.setCreatedBy(user);
        loan.setCreationDate(LocalDate.now());

        // Si todas las reglas se cumplen, se guarda el préstamo.
        loanPort.save(loan);

        // RN-20:
        // Registrar la creación de la solicitud en la bitácora.
        registerLoanCreatedLog(user, loan);
    }

    private void validateActiveUser(User user) {

        // Validación general:
        // El usuario actor no puede estar inactivo o bloqueado.
        if (user.getUserStatus() == UserStatus.INACTIVE || user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("El usuario debe estar activo para crear solicitudes de préstamo");
        }
    }

    private void validateActiveCustomer(Customer customer) {

        // RN-06:
        // El cliente asociado al préstamo debe estar activo.
        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("El cliente debe estar activo para solicitar un préstamo");
        }
    }

    private void validateAuthorizedRole(User user) {

        // RN-AD11:
        // Solo ciertos roles pueden crear solicitudes de préstamo.
        if (user.getSystemRole() != RoleType.INDIVIDUAL_CUSTOMER &&
            user.getSystemRole() != RoleType.BUSINESS_CUSTOMER &&
            user.getSystemRole() != RoleType.COMMERCIAL_EMPLOYEE) {
            throw new BusinessException("El usuario no tiene permisos para crear solicitudes de préstamo");
        }
    }

    private void validateActorAccessToCustomer(User user, Customer customer) {

        // RN-22 / RN-23:
        // Si el creador es un cliente, solo puede crear préstamos para sí mismo
        // o para su empresa asociada.
        if (user.getSystemRole() == RoleType.INDIVIDUAL_CUSTOMER ||
            user.getSystemRole() == RoleType.BUSINESS_CUSTOMER) {

            if (user.getCustomer() == null ||
                customer.getIdentificationNumber() == null ||
                !customer.getIdentificationNumber().equals(user.getCustomer().getIdentificationNumber())) {
                throw new BusinessException("El cliente solo puede crear solicitudes de préstamo para sus propios productos");
            }
            return;
        }

        // RN-27:
        // Si el creador es empleado comercial, el cliente debe estar asignado.
        if (user.getSystemRole() == RoleType.COMMERCIAL_EMPLOYEE) {
            if (user.getAssignedCustomers() == null || user.getAssignedCustomers().stream()
                .filter(assignedCustomer -> assignedCustomer != null)
                .noneMatch(assignedCustomer ->
                    customer.getIdentificationNumber().equals(assignedCustomer.getIdentificationNumber()))) {
                throw new BusinessException("El cliente no está asignado al empleado comercial");
            }
        }
    }

    private void validateLoanId(String loanId) {

        // Regla general del préstamo:
        // El ID del préstamo es obligatorio.
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }
    }

    private void validateUniqueLoanId(String loanId) {

        // Regla general del préstamo:
        // No puede existir otro préstamo con el mismo ID.
        Loan existingLoan = loanPort.findByLoanId(loanId);

        if (existingLoan != null) {
            throw new BusinessException("Ya existe un préstamo con ese ID");
        }
    }

    private void validateLoanType(Loan loan) {

        // Validación general:
        // El tipo de préstamo es obligatorio.
        if (loan.getLoanType() == null) {
            throw new BusinessException("El tipo de préstamo es obligatorio");
        }
    }

    private void validateRequestedAmount(BigDecimal requestedAmount) {

        // Regla general del préstamo:
        // El monto solicitado debe ser mayor que cero.
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto solicitado debe ser mayor que cero");
        }
    }

    private void validateInterestRate(BigDecimal interestRate) {

        // Regla general del préstamo:
        // La tasa de interés debe ser mayor que cero.
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La tasa de interés debe ser mayor que cero");
        }
    }

    private void validateTermMonths(int termMonths) {

        // Regla general del préstamo:
        // El plazo del préstamo en meses debe ser mayor que cero.
        if (termMonths <= 0) {
            throw new BusinessException("El plazo del préstamo debe ser mayor que cero");
        }
    }

    private void validateLoanCatalog(Loan loan) {

        // Regla general del préstamo:
        // El préstamo debe tener un producto del catálogo asociado.
        if (loan.getCatalog() == null) {
            throw new BusinessException("El préstamo debe estar asociado a un producto del catálogo");
        }

        // Regla general del préstamo:
        // El código del producto es obligatorio.
        if (loan.getCatalog().getProductCode() == null || loan.getCatalog().getProductCode().trim().isEmpty()) {
            throw new BusinessException("El código del producto del catálogo es obligatorio");
        }

        // Regla general del préstamo:
        // El nombre del producto es obligatorio.
        if (loan.getCatalog().getProductName() == null || loan.getCatalog().getProductName().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto del catálogo es obligatorio");
        }

        // Regla general del préstamo:
        // La categoría del catálogo debe corresponder a un préstamo.
        if (loan.getCatalog().getCategory() != ProductCategory.LOAN) {
            throw new BusinessException("El catálogo asociado no corresponde a un producto de tipo préstamo");
        }

        // Regla general del préstamo:
        // El producto del catálogo debe estar activo.
        if (!loan.getCatalog().isActive()) {
            throw new BusinessException("El producto del catálogo no se encuentra activo");
        }
    }

    private void registerLoanCreatedLog(User user, Loan loan) {

        // RN-20:
        // Registrar la creación de la solicitud de préstamo en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.LOAN_CREATED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(loan.getLoanId());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("loanId", loan.getLoanId());
        detailData.put("loanType", loan.getLoanType() != null ? loan.getLoanType().name() : null);
        detailData.put("requestedAmount", loan.getRequestedAmount());
        detailData.put("interestRate", loan.getInterestRate());
        detailData.put("termMonths", loan.getTermMonths());
        detailData.put("loanStatus", loan.getLoanStatus() != null ? loan.getLoanStatus().name() : null);
        detailData.put("ownerIdentification", loan.getOwner() != null ? loan.getOwner().getIdentificationNumber() : null);

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}
