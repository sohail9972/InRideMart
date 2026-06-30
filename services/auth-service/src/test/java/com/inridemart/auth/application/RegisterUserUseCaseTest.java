package com.inridemart.auth.application;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.Role;
import com.inridemart.auth.domain.UserAccount;
import com.inridemart.auth.domain.UserRegisteredEvent;
import com.inridemart.auth.domain.ports.DomainEventPublisher;
import com.inridemart.auth.domain.ports.PasswordHasher;
import com.inridemart.auth.domain.ports.RefreshTokenRepository;
import com.inridemart.auth.domain.ports.TokenService;
import com.inridemart.auth.domain.ports.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserUseCaseTest {
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final RegisterUserUseCase useCase = new RegisterUserUseCase(
            users,
            new PlainPasswordHasher(),
            new StaticTokenService(),
            new NoOpRefreshTokenRepository(),
            new NoOpEventPublisher(),
            Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void registersCustomer() {
        AuthResult result = useCase.register(new RegisterUserCommand("new@example.com", "StrongPass123", Role.CUSTOMER));

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.role()).isEqualTo(Role.CUSTOMER);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(users.existsByEmail(new EmailAddress("new@example.com"))).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() {
        useCase.register(new RegisterUserCommand("dupe@example.com", "StrongPass123", Role.CUSTOMER));

        assertThatThrownBy(() -> useCase.register(new RegisterUserCommand("dupe@example.com", "StrongPass123", Role.CUSTOMER)))
                .isInstanceOf(DuplicateEmailException.class);
    }

    private static class InMemoryUserRepository implements UserAccountRepository {
        private final Map<String, UserAccount> users = new HashMap<>();

        @Override
        public boolean existsByEmail(EmailAddress email) {
            return users.containsKey(email.value());
        }

        @Override
        public Optional<UserAccount> findByEmail(EmailAddress email) {
            return Optional.ofNullable(users.get(email.value()));
        }

        @Override
        public Optional<UserAccount> findById(UUID id) {
            return users.values().stream().filter(user -> user.id().equals(id)).findFirst();
        }

        @Override
        public UserAccount save(UserAccount userAccount) {
            users.put(userAccount.email().value(), userAccount);
            return userAccount;
        }
    }

    private static class PlainPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash.equals(hash(rawPassword));
        }
    }

    private static class StaticTokenService implements TokenService {
        @Override
        public String issueAccessToken(UserAccount user) {
            return "access-token";
        }

        @Override
        public String issueRefreshToken(UUID tokenId) {
            return "refresh-token";
        }

        @Override
        public String hashToken(String token) {
            return "hash:" + token;
        }
    }

    private static class NoOpRefreshTokenRepository implements RefreshTokenRepository {
        @Override
        public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        }
    }

    private static class NoOpEventPublisher implements DomainEventPublisher {
        @Override
        public void publish(UserRegisteredEvent event) {
        }
    }
}

