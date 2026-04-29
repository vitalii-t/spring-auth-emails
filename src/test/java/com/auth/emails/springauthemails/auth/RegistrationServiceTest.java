package com.auth.emails.springauthemails.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth.emails.springauthemails.user.User;
import com.auth.emails.springauthemails.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registersUserAndSendsVerificationEmail() {
        when(userRepository.existsByEmailIgnoreCase("[email protected]")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = registrationService.register("Demo User", "[email protected]", "password123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(verificationService).sendVerificationEmail(user);
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(captor.getValue().getEmail()).isEqualTo("[email protected]");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("[email protected]")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register("Demo User", "[email protected]", "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already registered");
    }
}
