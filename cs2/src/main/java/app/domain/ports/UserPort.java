package app.domain.ports;

import app.domain.models.person.User;

public interface UserPort {
    User findByIdentificationNumber(String identificationNumber);
    void save(User user);
    void update(User user);
}
