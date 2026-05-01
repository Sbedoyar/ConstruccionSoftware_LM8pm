package app.application.adapters.api.controllers;

import app.application.adapters.api.request.UserRequest;
import app.application.adapters.api.response.UserResponse;
import app.application.usecases.UserUseCase;
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

        return user;
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