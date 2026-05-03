package app.application.adapters.api.controllers;

import app.application.adapters.api.request.LoginRequest;
import app.application.adapters.api.response.LoginResponse;
import app.application.usecases.AuthUseCase;
import app.domain.models.auth.AuthSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = authUseCase.login(
                request.getUsername(),
                request.getPassword()
        );

        LoginResponse response = new LoginResponse(
                session.getToken(),
                session.getTokenType(),
                session.getIdentificationNumber(),
                session.getUsername(),
                session.getRole()
        );

        return ResponseEntity.ok(response);
    }
}