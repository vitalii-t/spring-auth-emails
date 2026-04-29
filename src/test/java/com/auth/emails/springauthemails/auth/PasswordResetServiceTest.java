package com.auth.emails.springauthemails.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth.emails.springauthemails.auth.PasswordResetService.PasswordResetResult;
import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.mail.AppMailService;
import com.auth.emails.springauthemails.user.User;
import com.auth.emails.springauthemails.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private AppMailService mailService;

    private PasswordResetService passwordResetService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-29T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
            userRepository,
            tokenRepository,
            passwordEncoder,
            tokenHasher,
            mailService,
            properties(),
            clock
        );
    }

    @Test
    void storesHashedTokenAndSendsEmailWhenUserExists() {
        User user = user(11L, "[email protected]");
        when(userRepository.findByEmailIgnoreCase("[email protected]")).thenReturn(Optional.of(user));
        when(tokenHasher.sha256(any(String.class))).thenReturn("hashed-token");

        passwordResetService.sendResetLink("[email protected]");

        verify(tokenRepository).deleteByUser(user);
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        verify(mailService).sendPasswordResetEmail(any(User.class), any(String.class), anyLong());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-token");
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(Instant.parse("2026-04-29T13:00:00Z"));
    }

    @Test
    void doesNothingForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("[email protected]")).thenReturn(Optional.empty());

        passwordResetService.sendResetLink("[email protected]");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(any(), any(), anyLong());
    }

    @Test
    void resetsPasswordWithValidToken() {
        User user = user(11L, "[email protected]");
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash("hashed-token");
        token.setExpiresAt(Instant.parse("2026-04-29T13:00:00Z"));

        when(userRepository.findByEmailIgnoreCase("[email protected]")).thenReturn(Optional.of(user));
        when(tokenHasher.sha256("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        PasswordResetResult result = passwordResetService.resetPassword("[email protected]", "raw-token", "new-password");

        assertThat(result).isEqualTo(PasswordResetResult.SUCCESS);
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        verify(tokenRepository).deleteByUser(user);
    }

    @Test
    void rejectsExpiredToken() {
        User user = user(11L, "[email protected]");
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash("hashed-token");
        token.setExpiresAt(Instant.parse("2026-04-29T11:00:00Z"));

        when(userRepository.findByEmailIgnoreCase("[email protected]")).thenReturn(Optional.of(user));
        when(tokenHasher.sha256("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        PasswordResetResult result = passwordResetService.resetPassword("[email protected]", "raw-token", "new-password");

        assertThat(result).isEqualTo(PasswordResetResult.INVALID_TOKEN);
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

    private User user(long id, String email) {
        User user = new User();
        user.setName("Demo User");
        user.setEmail(email);
        user.setPasswordHash("old-hash");
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return user;
    }
}
