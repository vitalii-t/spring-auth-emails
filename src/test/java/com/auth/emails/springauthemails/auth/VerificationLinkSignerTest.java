package com.auth.emails.springauthemails.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.user.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class VerificationLinkSignerTest {

    @Test
    void createsAndValidatesSignedLink() {
        VerificationLinkSigner signer = new VerificationLinkSigner(properties(), fixedClock());

        User user = user();
        VerificationLinkSigner.SignedVerificationLink link = signer.createFor(user);

        assertThat(signer.isValid(user, link.expiresAt(), link.signature())).isTrue();
    }

    @Test
    void rejectsExpiredLink() {
        VerificationLinkSigner signer = new VerificationLinkSigner(properties(), fixedClock());

        User user = user();
        VerificationLinkSigner.SignedVerificationLink link = signer.createFor(user);
        Clock later = Clock.fixed(Instant.parse("2026-04-29T13:05:01Z"), ZoneOffset.UTC);
        VerificationLinkSigner laterSigner = new VerificationLinkSigner(properties(), later);

        assertThat(laterSigner.isValid(user, link.expiresAt(), link.signature())).isFalse();
    }

    @Test
    void rejectsTamperedSignature() {
        VerificationLinkSigner signer = new VerificationLinkSigner(properties(), fixedClock());

        User user = user();
        VerificationLinkSigner.SignedVerificationLink link = signer.createFor(user);

        assertThat(signer.isValid(user, link.expiresAt(), "bad-signature")).isFalse();
    }

    private AppProperties properties() {
        return new AppProperties(
            "http://localhost:8080",
            60,
            60,
            new AppProperties.Security("test-secret"),
            new AppProperties.Mail(null, "[email protected]", "Test", false, 0)
        );
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-04-29T12:00:00Z"), ZoneOffset.UTC);
    }

    private User user() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("[email protected]");
        user.setPasswordHash("hash");
        setId(user, 7L);
        return user;
    }

    private void setId(User user, long id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
