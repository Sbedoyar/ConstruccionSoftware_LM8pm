package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.bankingProduct.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.RoleType;
import app.domain.models.person.User;
import app.domain.ports.LoanPort;
import app.domain.ports.UserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RejectLoan {

    private final LoanPort loanPort;
    private final UserPort userPort;

    @Autowired
    public RejectLoan(LoanPort loanPort, UserPort userPort) {
        this.loanPort = loanPort;
        this.userPort = userPort;
    }

    public void rejectLoan(String loanId, String userIdentification) throws BusinessException {

        // Validación general:
        // El ID del préstamo es obligatorio.
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new BusinessException("El ID del préstamo es obligatorio");
        }

        // Se busca el préstamo a rechazar.
        Loan loan = loanPort.findByLoanId(loanId.trim());
        if (loan == null) {
            throw new BusinessException("No existe un préstamo con ese ID");
        }

        // Se busca el usuario que realiza el rechazo.
        User user = userPort.findByIdentificationNumber(userIdentification);
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

        // Se actualiza el estado del préstamo a rechazado.
        loan.setLoanStatus(LoanStatus.REJECTED);

        // Se registra quién rechazó y cuándo lo hizo.
        loan.setReviewedBy(user);
        loan.setReviewDate(LocalDate.now());

        // Se actualiza el préstamo.
        loanPort.update(loan);
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
}