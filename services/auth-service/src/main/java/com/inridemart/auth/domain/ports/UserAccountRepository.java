package com.inridemart.auth.domain.ports;

import com.inridemart.auth.domain.EmailAddress;
import com.inridemart.auth.domain.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {
    boolean existsByEmail(EmailAddress email);

    Optional<UserAccount> findByEmail(EmailAddress email);

    Optional<UserAccount> findById(UUID id);

    UserAccount save(UserAccount userAccount);
}

