package app.domain.models.bankingProduct;

import app.domain.models.enums.ProductCategory;
import app.domain.models.user.Customer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public abstract class BankingProduct {
    private Customer owner;
    private String productCode;
    private String productName;
    private ProductCategory category;
    private boolean requiresApproval;

}
