package app.domain.models.user;

import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class User {
    private long userId;
    private Customer relatedCustomer;
    private String username;
    private String password;
    private RoleType systemRole;
    private UserStatus userStatus;

}
