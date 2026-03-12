package app.domain.models.person;

import java.time.LocalDate;
import java.util.List;

import app.domain.models.bankingProduct.BankingProduct;
import app.domain.models.enums.CustomerStatus;
import app.domain.models.enums.RoleTypeCustomer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public abstract class Customer extends Person{
    private LocalDate registrationDate;
    private CustomerStatus customerStatus;
    private RoleTypeCustomer role;
    private List<BankingProduct> bankingProducts;
}
