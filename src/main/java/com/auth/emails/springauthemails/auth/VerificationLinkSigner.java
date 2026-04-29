package com.auth.emails.springauthemails.auth;

import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class VerificationLinkSigner {

    private final AppProperties appProperties;
    private final Clock clock;

    public VerificationLinkSigner(AppProperties appProperties, Clock clock) {
        this.appProperties = appProperties;
        this.clock = clock;
    }

    public SignedVerificationLink createFor(User user) {
        long expiresAt = Instant.now(clock)
            .plusSeconds(appProperties.verificationTtlMinutes() * 60)
            .getEpochSecond();
        String signature = sign(payload(user.getId(), user.getEmail(), expiresAt));
        return new SignedVerificationLink(user.getId(), expiresAt, signature);
    }

    public boolean isValid(User user, long expiresAt, String signature) {
        long now = Instant.now(clock).getEpochSecond();
        if (expiresAt < now) {
            return false;
        }
        String expected = sign(payload(user.getId(), user.getEmail(), expiresAt));
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String payload(Long userId, String email, long expiresAt) {
        return userId + ":" + email.toLowerCase() + ":" + expiresAt;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                appProperties.security().signingSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            ));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign verification link", exception);
        }
    }

    public record SignedVerificationLink(Long userId, long expiresAt, String signature) {
    }
}
