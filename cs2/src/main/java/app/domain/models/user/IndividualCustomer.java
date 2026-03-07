package app.domain.models.user;

import java.time.LocalDate;
import app.domain.models.enums.RoleTypeCustomer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class IndividualCustomer extends Customer{
    private LocalDate dateOfBirth;
    private RoleTypeCustomer role;


}
