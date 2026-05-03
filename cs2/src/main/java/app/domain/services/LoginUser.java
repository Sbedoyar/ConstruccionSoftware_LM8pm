package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

@Service
public class LoginUser {

    private final UserPort userPort;

    public LoginUser(UserPort userPort) {
        this.userPort = userPort;
    }

    public User login(String username, String password) throws BusinessException {

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("El username es obligatorio");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria");
        }

        User user = userPort.findByUsername(username.trim());

        if (user == null) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        if (!password.equals(user.getPassword())) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo");
        }

        return user;
    }
}