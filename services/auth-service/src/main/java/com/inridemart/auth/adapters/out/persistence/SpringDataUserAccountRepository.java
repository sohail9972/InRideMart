package com.inridemart.auth.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserAccountEntity> findByEmail(String email);
}

