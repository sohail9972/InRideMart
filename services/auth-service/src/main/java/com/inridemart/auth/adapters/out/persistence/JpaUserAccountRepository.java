package com.inridemart.auth.adapters.out.persistence;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.PasswordHash;
import com.inridemart.auth.domain.UserAccount;
import com.inridemart.auth.domain.ports.UserAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserAccountRepository implements UserAccountRepository {
    private final SpringDataUserAccountRepository repository;

    public JpaUserAccountRepository(SpringDataUserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress email) {
        return repository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        return toDomain(repository.save(toEntity(userAccount)));
    }

    private UserAccountEntity toEntity(UserAccount user) {
        return new UserAccountEntity(user.id(), user.email().value(), user.passwordHash().value(), user.role(), user.status(), user.createdAt(), user.updatedAt());
    }

    private UserAccount toDomain(UserAccountEntity entity) {
        return UserAccount.rehydrate(
                entity.getId(),
                new EmailAddress(entity.getEmail()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

