package com.inridemart.auth.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {
    @Test
    void normalizesEmail() {
        EmailAddress email = new EmailAddress(" USER@Example.COM ");

        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> new EmailAddress("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is invalid");
    }
}

