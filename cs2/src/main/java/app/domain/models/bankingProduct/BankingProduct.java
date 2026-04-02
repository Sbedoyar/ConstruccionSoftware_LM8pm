package app.domain.models.bankingProduct;

import app.domain.models.person.Customer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public abstract class BankingProduct {
    private Customer owner;
    private BankProductCatalog catalog;

}
