package com.inridemart.auth.adapters.in.web;

import com.inridemart.auth.application.LoginCommand;
import com.inridemart.auth.application.LoginUserUseCase;
import com.inridemart.auth.application.RegisterUserCommand;
import com.inridemart.auth.application.RegisterUserUseCase;
import com.inridemart.auth.domain.ports.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UserAccountRepository users;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase, UserAccountRepository users) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.users = users;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = AuthResponse.from(registerUserUseCase.register(new RegisterUserCommand(request.email(), request.password(), request.role())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(loginUserUseCase.login(new LoginCommand(request.email(), request.password())));
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return users.findById(userId)
                .map(user -> new UserProfileResponse(user.id(), user.email().value(), user.role()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

