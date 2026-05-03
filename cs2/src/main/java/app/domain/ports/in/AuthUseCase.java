package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.auth.AuthSession;

public interface AuthUseCase {

    AuthSession login(String username, String password) throws BusinessException;
}