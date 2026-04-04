package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.LoanPort;
import app.domain.ports.OperationLogPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApproveLoan {

    private final LoanPort loanPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    @Autowired
    public ApproveLoan(LoanPort loanPort, UserPort userPort, OperationLogPort operationLogPort) {
        this.loanPort = loanPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    public void approveLoan(String loanId, String userIdentification) throws BusinessException {

        // Validación general:
        // El ID del préstamo es obligatorio.
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        // Validación general:
        // La identificación del usuario aprobador es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca el préstamo a aprobar.
        Loan loan = loanPort.findByLoanId(loanId.trim());
        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        // Se busca el usuario que realiza la aprobación.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        // RN-08 / RN-35:
        // Solo el Analista Interno puede aprobar o rechazar préstamos
        // y es el rol primario para modificar su estado.
        validateInternalAnalyst(user);

        // RN-07:
        // Un préstamo solo puede pasar de "En estudio" a "Aprobado" o "Rechazado".
        validateLoanInReview(loan);

        // RN-11:
        // Para aprobar formalmente, el monto aprobado debe ser mayor que cero.
        validateApprovedAmount(loan);

        // Se actualiza el estado del préstamo a aprobado.
        loan.setLoanStatus(LoanStatus.APPROVED);

        // Se registra quién aprobó y cuándo lo hizo.
        loan.setReviewedBy(user);
        loan.setReviewDate(LocalDate.now());

        // Se actualiza el préstamo.
        loanPort.update(loan);

        // RN-20:
        // Registrar la aprobación del préstamo en la bitácora.
        registerLoanApprovedLog(user, loan);
    }

    private void validateInternalAnalyst(User user) {

        // RN-08 / RN-35:
        // Solo el rol INTERNAL_ANALYST puede aprobar préstamos.
        if (user.getSystemRole() != RoleType.INTERNAL_ANALYST) {
            throw new BusinessException("Solo un analista interno puede aprobar préstamos");
        }
    }

    private void validateLoanInReview(Loan loan) {

        // RN-07:
        // Solo se puede aprobar un préstamo que esté en estado IN_REVIEW.
        if (loan.getLoanStatus() != LoanStatus.IN_REVIEW) {
            throw new BusinessException("Solo se puede aprobar un préstamo en estado En estudio");
        }
    }

    private void validateApprovedAmount(Loan loan) {

        // RN-11:
        // El monto aprobado debe ser mayor que cero.
        if (loan.getApprovedAmount() == null || loan.getApprovedAmount().signum() <= 0) {
            throw new BusinessException("Para aprobar el préstamo, el monto aprobado debe ser mayor que cero");
        }
    }

    private void registerLoanApprovedLog(User user, Loan loan) {

        // RN-20:
        // Registrar la aprobación del préstamo en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.LOAN_APPROVED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(loan.getLoanId());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("loanId", loan.getLoanId());
        detailData.put("approvedAmount", loan.getApprovedAmount());
        detailData.put("previousStatus", LoanStatus.IN_REVIEW.name());
        detailData.put("newStatus", LoanStatus.APPROVED.name());

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}
