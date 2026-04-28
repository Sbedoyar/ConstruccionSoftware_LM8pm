package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
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

//@Service
public class RejectLoan {

    private final LoanPort loanPort;
    private final UserPort userPort;
    private final OperationLogPort operationLogPort;

    //@Autowired
    public RejectLoan(LoanPort loanPort, UserPort userPort, OperationLogPort operationLogPort) {
        this.loanPort = loanPort;
        this.userPort = userPort;
        this.operationLogPort = operationLogPort;
    }

    public void rejectLoan(String loanId, String userIdentification) throws BusinessException {

        // Validación general:
        // El ID del préstamo es obligatorio.
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        // Validación general:
        // La identificación del usuario que rechaza es obligatoria.
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            throw new BusinessException("La identificación del usuario es obligatoria");
        }

        // Se busca el préstamo a rechazar.
        Loan loan = loanPort.findByLoanId(loanId.trim());
        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        // Se busca el usuario que realiza el rechazo.
        User user = userPort.findByIdentificationNumber(userIdentification.trim());
        if (user == null) {
            throw new BusinessException("No existe un usuario con esa identificación");
        }

        validateActiveUser(user);

        // RN-08 / RN-35:
        // Solo el Analista Interno puede aprobar o rechazar préstamos
        // y es el rol primario para modificar su estado.
        validateInternalAnalyst(user);

        // RN-07:
        // Un préstamo solo puede pasar de "En estudio" a "Aprobado" o "Rechazado".
        validateLoanInReview(loan);

        // Se actualiza el estado del préstamo a rechazado.
        loan.setLoanStatus(LoanStatus.REJECTED);

        // Se registra quién rechazó y cuándo lo hizo.
        loan.setReviewedBy(user);
        loan.setReviewDate(LocalDate.now());

        // Se actualiza el préstamo.
        loanPort.update(loan);

        // RN-20:
        // Registrar el rechazo del préstamo en la bitácora.
        registerLoanRejectedLog(user, loan);
    }

    private void validateActiveUser(User user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo para realizar esta operación");
        }
    }

    private void validateInternalAnalyst(User user) {

        // RN-08 / RN-35:
        // Solo el rol INTERNAL_ANALYST puede rechazar préstamos.
        if (user.getSystemRole() != RoleType.INTERNAL_ANALYST) {
            throw new BusinessException("Solo un analista interno puede rechazar préstamos");
        }
    }

    private void validateLoanInReview(Loan loan) {

        // RN-07:
        // Solo se puede rechazar un préstamo que esté en estado IN_REVIEW.
        if (loan.getLoanStatus() != LoanStatus.IN_REVIEW) {
            throw new BusinessException("Solo se puede rechazar un préstamo en estado En estudio");
        }
    }

    private void registerLoanRejectedLog(User user, Loan loan) {

        // RN-20:
        // Registrar el rechazo del préstamo en la bitácora.
        OperationLog operationLog = new OperationLog();
        operationLog.setLogId("LOG-" + System.currentTimeMillis());
        operationLog.setOperationType(OperationType.LOAN_REJECTED);
        operationLog.setTimestamp(LocalDateTime.now());
        operationLog.setUser(user);
        operationLog.setUserRole(user.getSystemRole());
        operationLog.setAffectedProductId(loan.getLoanId());

        Map<String, Object> detailData = new HashMap<>();
        detailData.put("loanId", loan.getLoanId());
        detailData.put("previousStatus", LoanStatus.IN_REVIEW.name());
        detailData.put("newStatus", LoanStatus.REJECTED.name());

        operationLog.setDetailData(detailData);

        operationLogPort.save(operationLog);
    }
}
