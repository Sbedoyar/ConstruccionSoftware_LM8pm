package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.auth.AuthSession;
import app.domain.models.person.User;
import app.domain.services.LoginUser;
import app.infrastructure.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthUseCase implements app.domain.ports.in.AuthUseCase {

    private final LoginUser loginUser;
    private final JwtUtil jwtUtil;

    public AuthUseCase(LoginUser loginUser, JwtUtil jwtUtil) {
        this.loginUser = loginUser;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthSession login(String username, String password) throws BusinessException {
        User user = loginUser.login(username, password);

        String token = jwtUtil.generateToken(user);

        return new AuthSession(
                token,
                "Bearer",
                user.getIdentificationNumber(),
                user.getUsername(),
                user.getSystemRole()
        );
    }
}