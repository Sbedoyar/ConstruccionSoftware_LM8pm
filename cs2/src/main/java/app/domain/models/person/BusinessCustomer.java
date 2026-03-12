package app.domain.models.person;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class BusinessCustomer extends Customer{
    private IndividualCustomer legalRepresentative;

}
