package com.auth.emails.springauthemails.auth;

import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.mail.AppMailService;
import com.auth.emails.springauthemails.user.User;
import com.auth.emails.springauthemails.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final VerificationLinkSigner verificationLinkSigner;
    private final AppMailService mailService;
    private final AppProperties appProperties;
    private final Clock clock;

    public void sendVerificationEmail(User user) {
        VerificationLinkSigner.SignedVerificationLink link = verificationLinkSigner.createFor(user);
        String url = appProperties.baseUrl()
            + "/verify-email/confirm?uid=" + link.userId()
            + "&expires=" + link.expiresAt()
            + "&signature=" + link.signature();
        mailService.sendVerificationEmail(user, url);
    }

    @Transactional
    public boolean verifyUser(long userId, long expiresAt, String signature) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !verificationLinkSigner.isValid(user, expiresAt, signature)) {
            return false;
        }

        if (!user.isVerified()) {
            user.setEmailVerifiedAt(Instant.now(clock));
        }

        return true;
    }
}
