package app.application.adapters.persistence.sql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_product_catalog")
public class BankProductCatalogEntity {

    @Id
    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @Column(name = "active", nullable = false)
    private boolean active;
}