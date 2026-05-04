package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.UserPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUser {

    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;

    public LoginUser(UserPort userPort, PasswordEncoder passwordEncoder) {
        this.userPort = userPort;
        this.passwordEncoder = passwordEncoder;
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

        validatePassword(password, user);

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("El usuario no está activo");
        }

        return user;
    }

    private void validatePassword(String rawPassword, User user) {

        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        if (isBCryptPassword(storedPassword)) {
            if (!passwordEncoder.matches(rawPassword, storedPassword)) {
                throw new BusinessException("Usuario o contraseña incorrectos");
            }
            return;
        }

        if (!rawPassword.equals(storedPassword)) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userPort.update(user);
    }

    private boolean isBCryptPassword(String password) {
        return password.startsWith("$2a$") ||
                password.startsWith("$2b$") ||
                password.startsWith("$2y$");
    }
}