package app.domain.models.bankingProduct;

import app.domain.models.enums.ProductCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class BankProductCatalog {
    private String productCode;
    private String productName;
    private String description;
    private ProductCategory category;
    private boolean requiresApproval;
    private boolean active;
}
