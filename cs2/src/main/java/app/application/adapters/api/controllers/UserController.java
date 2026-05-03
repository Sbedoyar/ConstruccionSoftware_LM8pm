package app.application.adapters.api.controllers;

import app.application.adapters.api.request.UserRequest;
import app.application.adapters.api.response.UserResponse;
import app.application.usecases.UserUseCase;
import app.domain.models.enums.RoleType;
import app.domain.models.person.BusinessCustomer;
import app.domain.models.person.IndividualCustomer;
import app.domain.models.person.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = toUser(request);

        userUseCase.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toUserResponse(user));
    }

    private static User toUser(UserRequest request) {
        User user = new User();

        user.setIdentificationNumber(request.getIdentificationNumber());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setSystemRole(request.getSystemRole());
        user.setUserStatus(request.getUserStatus());
        assignCustomerToUser(user, request.getCustomerIdentification());

        return user;
    }

    private static void assignCustomerToUser(User user, String customerIdentification) {
        if (customerIdentification == null || customerIdentification.trim().isEmpty()) {
            return;
        }

        if (user.getSystemRole() == RoleType.INDIVIDUAL_CUSTOMER) {
            IndividualCustomer customer = new IndividualCustomer();
            customer.setIdentificationNumber(customerIdentification.trim());
            user.setCustomer(customer);
            return;
        }

        if (user.getSystemRole() == RoleType.BUSINESS_CUSTOMER ||
                user.getSystemRole() == RoleType.COMPANY_SUPERVISOR ||
                user.getSystemRole() == RoleType.PENDING_COMPANY_OPERATOR) {

            BusinessCustomer customer = new BusinessCustomer();
            customer.setIdentificationNumber(customerIdentification.trim());
            user.setCustomer(customer);
        }
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getIdentificationNumber(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getUsername(),
                user.getSystemRole(),
                user.getUserStatus()
        );
    }
}