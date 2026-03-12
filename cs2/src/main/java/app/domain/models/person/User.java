package app.domain.models.person;

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

}
