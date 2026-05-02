package app.application.adapters.persistence.sql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Integer transferId;

    @Column(name = "source_account_number", nullable = false)
    private String sourceAccountNumber;

    @Column(name = "target_account_number")
    private String targetAccountNumber;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_by_identification", nullable = false)
    private String createdByIdentification;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "reviewed_by_identification")
    private String reviewedByIdentification;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "transfer_type", nullable = false)
    private String transferType;
}
