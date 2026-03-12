package app.domain.models.person;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class IndividualCustomer extends Customer{
    private LocalDate dateOfBirth;
    
}
