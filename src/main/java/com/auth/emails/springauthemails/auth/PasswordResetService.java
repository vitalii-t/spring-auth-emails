package com.auth.emails.springauthemails.auth;

import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.mail.AppMailService;
import com.auth.emails.springauthemails.user.User;
import com.auth.emails.springauthemails.user.UserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHasher tokenHasher;
    private final AppMailService mailService;
    private final AppProperties appProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendResetLink(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return;
        }

        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(tokenHasher.sha256(rawToken));
        token.setExpiresAt(Instant.now(clock).plusSeconds(appProperties.passwordResetTtlMinutes() * 60));
        passwordResetTokenRepository.save(token);

        String url = appProperties.baseUrl()
            + "/reset-password?token=" + rawToken
            + "&email=" + user.getEmail();
        mailService.sendPasswordResetEmail(user, url, appProperties.passwordResetTtlMinutes());
    }

    @Transactional
    public PasswordResetResult resetPassword(String email, String rawToken, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return PasswordResetResult.INVALID_TOKEN;
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHasher.sha256(rawToken))
            .orElse(null);

        if (token == null || !token.getUser().getId().equals(user.getId()) || !token.isUsableAt(Instant.now(clock))) {
            return PasswordResetResult.INVALID_TOKEN;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now(clock));
        passwordResetTokenRepository.deleteByUser(user);
        return PasswordResetResult.SUCCESS;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public enum PasswordResetResult {
        SUCCESS,
        INVALID_TOKEN
    }
}
