package com.inridemart.auth.application;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.PasswordHash;
import com.inridemart.auth.domain.Role;
import com.inridemart.auth.domain.UserAccount;
import com.inridemart.auth.domain.ports.PasswordHasher;
import com.inridemart.auth.domain.ports.RefreshTokenRepository;
import com.inridemart.auth.domain.ports.TokenService;
import com.inridemart.auth.domain.ports.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUserUseCaseTest {
    private final UserAccount user = UserAccount.register(new EmailAddress("user@example.com"), new PasswordHash("hashed:SecretPass123"), Role.CUSTOMER, Instant.now());
    private final LoginUserUseCase useCase = new LoginUserUseCase(
            new SingleUserRepository(user),
            new TestPasswordHasher(),
            new TestTokenService(),
            new TestRefreshTokenRepository(),
            Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void logsInWithValidCredentials() {
        AuthResult result = useCase.login(new LoginCommand("user@example.com", "SecretPass123"));

        assertThat(result.userId()).isEqualTo(user.id());
        assertThat(result.accessToken()).isEqualTo("access");
    }

    @Test
    void rejectsInvalidCredentials() {
        assertThatThrownBy(() -> useCase.login(new LoginCommand("user@example.com", "wrong")))
                .isInstanceOf(AuthenticationException.class);
    }

    private record SingleUserRepository(UserAccount user) implements UserAccountRepository {
        @Override
        public boolean existsByEmail(EmailAddress email) {
            return user.email().equals(email);
        }

        @Override
        public Optional<UserAccount> findByEmail(EmailAddress email) {
            return user.email().equals(email) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public Optional<UserAccount> findById(UUID id) {
            return user.id().equals(id) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public UserAccount save(UserAccount userAccount) {
            return userAccount;
        }
    }

    private static class TestPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash.equals(hash(rawPassword));
        }
    }

    private static class TestTokenService implements TokenService {
        @Override
        public String issueAccessToken(UserAccount user) {
            return "access";
        }

        @Override
        public String issueRefreshToken(UUID tokenId) {
            return "refresh";
        }

        @Override
        public String hashToken(String token) {
            return "hash:" + token;
        }
    }

    private static class TestRefreshTokenRepository implements RefreshTokenRepository {
        @Override
        public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        }
    }
}

