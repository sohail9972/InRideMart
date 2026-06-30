package com.inridemart.auth.application;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.UserAccount;
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
public class LoginUserUseCase {
    private final UserAccountRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokens;
    private final Clock clock;

    public LoginUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher, TokenService tokenService, RefreshTokenRepository refreshTokens, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokens = refreshTokens;
        this.clock = clock;
    }

    @Transactional
    public AuthResult login(LoginCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        UserAccount user = users.findByEmail(email)
                .filter(UserAccount::canAuthenticate)
                .filter(found -> passwordHasher.matches(command.password(), found.passwordHash().value()))
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        return issueTokens(user);
    }

    private AuthResult issueTokens(UserAccount user) {
        UUID refreshTokenId = UUID.randomUUID();
        Instant now = Instant.now(clock);
        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(refreshTokenId);
        refreshTokens.save(refreshTokenId, user.id(), tokenService.hashToken(refreshToken), now.plus(Duration.ofDays(30)), now);
        return new AuthResult(user.id(), user.email().value(), user.role(), accessToken, refreshToken, "Bearer", Duration.ofMinutes(15).toSeconds());
    }
}

