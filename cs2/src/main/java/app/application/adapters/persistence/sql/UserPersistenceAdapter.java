package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.UserEntity;
import app.application.adapters.persistence.sql.repositories.UserRepository;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.User;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

@Service
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository repository;

    public UserPersistenceAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User findByIdentificationNumber(String identificationNumber) {
        UserEntity entity = repository.findByIdentificationNumber(identificationNumber);
        return toModel(entity);
    }
    @Override
    public User findByUsername(String username) {
        UserEntity entity = repository.findByUsername(username);
        return toModel(entity);
    }

    @Override
    public void save(User user) {
        repository.save(toEntity(user));
    }

    @Override
    public void update(User user) {
        repository.save(toEntity(user));
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();

        entity.setId(user.getId());
        entity.setIdentificationNumber(user.getIdentificationNumber());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setAddress(user.getAddress());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());

        if (user.getSystemRole() != null) {
            entity.setSystemRole(user.getSystemRole().name());
        }

        if (user.getUserStatus() != null) {
            entity.setUserStatus(user.getUserStatus().name());
        }

        return entity;
    }

    private User toModel(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User();

        user.setId(entity.getId());
        user.setIdentificationNumber(entity.getIdentificationNumber());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setAddress(entity.getAddress());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());

        if (entity.getSystemRole() != null) {
            user.setSystemRole(RoleType.valueOf(entity.getSystemRole()));
        }

        if (entity.getUserStatus() != null) {
            user.setUserStatus(UserStatus.valueOf(entity.getUserStatus()));
        }

        return user;
    }
}