package app.application.usecases;

import app.domain.exceptions.BusinessException;
import app.domain.models.person.User;
import app.domain.services.CreateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserUseCase implements app.domain.ports.in.UserUseCase {

    @Autowired
    private CreateUser createUser;

    public UserUseCase(CreateUser createUser)  throws BusinessException {
        this.createUser = createUser;
    }

    @Override
    public void createUser(User user)  throws BusinessException {
        createUser.createUser(user);
    }
}