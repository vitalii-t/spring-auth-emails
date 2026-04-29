package com.auth.emails.springauthemails.mail;

import com.auth.emails.springauthemails.user.User;

public interface AppMailService {

    void sendVerificationEmail(User user, String verificationUrl);

    void sendPasswordResetEmail(User user, String resetUrl, long ttlMinutes);
}
