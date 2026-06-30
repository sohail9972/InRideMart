package com.inridemart.auth.application;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.PasswordHash;
import com.inridemart.auth.domain.Role;
import com.inridemart.auth.domain.UserAccount;
import com.inridemart.auth.domain.UserRegisteredEvent;
import com.inridemart.auth.domain.ports.DomainEventPublisher;
import com.inridemart.auth.domain.ports.PasswordHasher;
import com.inridemart.auth.domain.ports.RefreshTokenRepository;
import com.inridemart.auth.domain.ports.TokenService;
import com.inridemart.auth.domain.ports.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterUserUseCase {
    private final UserAccountRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokens;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    public RegisterUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher, TokenService tokenService, RefreshTokenRepository refreshTokens, DomainEventPublisher eventPublisher, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokens = refreshTokens;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public AuthResult register(RegisterUserCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException("Email is already registered");
        }
        validatePassword(command.password());

        Role role = command.role() == null ? Role.CUSTOMER : command.role();
        Instant now = Instant.now(clock);
        UserAccount user = UserAccount.register(email, new PasswordHash(passwordHasher.hash(command.password())), role, now);
        UserAccount saved = users.save(user);

        eventPublisher.publish(new UserRegisteredEvent(saved.id(), saved.email().value(), saved.role(), now));
        return issueTokens(saved, now);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentException("Password must contain at least 10 characters");
        }
    }

    private AuthResult issueTokens(UserAccount user, Instant now) {
        UUID refreshTokenId = UUID.randomUUID();
        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(refreshTokenId);
        refreshTokens.save(refreshTokenId, user.id(), tokenService.hashToken(refreshToken), now.plus(Duration.ofDays(30)), now);
        return new AuthResult(user.id(), user.email().value(), user.role(), accessToken, refreshToken, "Bearer", Duration.ofMinutes(15).toSeconds());
    }
}

