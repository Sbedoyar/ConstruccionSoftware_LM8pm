package app.domain.models.user;

import java.time.LocalDate;
import app.domain.models.enums.CustomerStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public abstract class Customer {
    private long customerId;
    private String fullName;
    private String identificationNumber;
    private String email;
    private String phone;
    private String address;
    private LocalDate registrationDate;
    private CustomerStatus customerStatus;
}
