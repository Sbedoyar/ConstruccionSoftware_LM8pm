package app.domain.ports.out;

import app.domain.models.person.User;

public interface UserPort {
    User findByIdentificationNumber(String identificationNumber);
    User findByUsername(String username);
    void save(User user);
    void update(User user);
}
