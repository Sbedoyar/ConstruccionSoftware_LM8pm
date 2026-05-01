package app.domain.ports.in;

import app.domain.exceptions.BusinessException;
import app.domain.models.person.User;

public interface UserUseCase {

    void createUser(User user) throws BusinessException;
}