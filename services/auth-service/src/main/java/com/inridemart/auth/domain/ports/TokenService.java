package com.inridemart.auth.domain.ports;

import com.inridemart.auth.domain.UserAccount;

import java.util.UUID;

public interface TokenService {
    String issueAccessToken(UserAccount user);

    String issueRefreshToken(UUID tokenId);

    String hashToken(String token);
}

