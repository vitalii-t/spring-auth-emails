package com.auth.emails.springauthemails.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthForms {

    private AuthForms() {
    }

    public record RegisterForm(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String confirmPassword
    ) {
        public RegisterForm() {
            this("", "", "", "");
        }
    }

    public record ForgotPasswordForm(@NotBlank @Email String email) {
        public ForgotPasswordForm() {
            this("");
        }
    }

    public record ResetPasswordForm(
        @NotBlank String token,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String confirmPassword
    ) {
        public ResetPasswordForm() {
            this("", "", "", "");
        }
    }
}
