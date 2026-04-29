package com.auth.emails.springauthemails.mail;

import com.auth.emails.springauthemails.config.AppProperties;
import com.auth.emails.springauthemails.user.User;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailtrapMailService implements AppMailService {

    private final MailtrapClient mailtrapClient;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("verificationUrl", verificationUrl);
        String html = templateEngine.process("mail/verify-email", context);
        send(user.getEmail(), "Verify your email", html);
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetUrl, long ttlMinutes) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("ttlMinutes", ttlMinutes);
        String html = templateEngine.process("mail/reset-password", context);
        send(user.getEmail(), "Reset your password", html);
    }

    private void send(String recipient, String subject, String html) {
        MailtrapMail mail = MailtrapMail.builder()
            .from(new Address(appProperties.mail().fromEmail()))
            .to(List.of(new Address(recipient)))
            .subject(subject)
            .html(html)
            .build();

        try {
            mailtrapClient.send(mail);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not send email via Mailtrap", exception);
        }
    }
}
