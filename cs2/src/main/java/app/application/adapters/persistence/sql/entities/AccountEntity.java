package app.application.adapters.persistence.sql.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "account_status", nullable = false)
    private String accountStatus;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "owner_identification", nullable = false)
    private String ownerIdentification;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @Column(name = "catalog_active", nullable = false)
    private boolean catalogActive;
}