package app.application.adapters.persistence.sql;

import app.application.adapters.persistence.sql.entities.UserEntity;
import app.application.adapters.persistence.sql.repositories.UserRepository;
import app.domain.models.enums.RoleType;
import app.domain.models.enums.UserStatus;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import app.domain.ports.out.UserPort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        UserEntity savedEntity = repository.save(toEntity(user));
        user.setId(savedEntity.getId());
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

        if (user.getAssignedCustomers() != null && !user.getAssignedCustomers().isEmpty()) {
            String assignedCustomerIdentifications = user.getAssignedCustomers()
                    .stream()
                    .filter(customer -> customer != null && customer.getIdentificationNumber() != null)
                    .map(Customer::getIdentificationNumber)
                    .collect(Collectors.joining(","));

            entity.setAssignedCustomerIdentifications(assignedCustomerIdentifications);
        } else {
            entity.setAssignedCustomerIdentifications(null);
        }

        if (user.getCustomer() != null) {
            entity.setCustomerIdentification(user.getCustomer().getIdentificationNumber());

            if (user.getCustomer() instanceof BusinessCustomer) {
                entity.setCustomerType("BUSINESS");
            } else if (user.getCustomer() instanceof IndividualCustomer) {
                entity.setCustomerType("INDIVIDUAL");
            }
        } else {
            entity.setCustomerIdentification(null);
            entity.setCustomerType(null);
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

        if (entity.getAssignedCustomerIdentifications() != null &&
                !entity.getAssignedCustomerIdentifications().trim().isEmpty()) {

            List<Customer> assignedCustomers = Arrays.stream(entity.getAssignedCustomerIdentifications().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(identification -> {
                        IndividualCustomer customer = new IndividualCustomer();
                        customer.setIdentificationNumber(identification);
                        return customer;
                    })
                    .collect(Collectors.toList());

            user.setAssignedCustomers(assignedCustomers);
        }

        if (entity.getCustomerIdentification() != null &&
                !entity.getCustomerIdentification().trim().isEmpty()) {

            if ("BUSINESS".equals(entity.getCustomerType())) {
                BusinessCustomer customer = new BusinessCustomer();
                customer.setIdentificationNumber(entity.getCustomerIdentification());
                user.setCustomer(customer);
            } else if ("INDIVIDUAL".equals(entity.getCustomerType())) {
                IndividualCustomer customer = new IndividualCustomer();
                customer.setIdentificationNumber(entity.getCustomerIdentification());
                user.setCustomer(customer);
            }
        }

        return user;
    }
}