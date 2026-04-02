package app.domain.models.person;

import java.util.List;

import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class User extends Person{
    private String username;
    private String password;
    private RoleType systemRole;
    private UserStatus userStatus;

    // Para clientes
    private Customer customer;

    // Para empleados del banco
    private List<Customer> assignedCustomers;

    // empleados de empresa
    private BusinessCustomer company; 

}
