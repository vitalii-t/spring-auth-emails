package com.auth.emails.springauthemails.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @NotBlank String baseUrl,
    @Positive long verificationTtlMinutes,
    @Positive long passwordResetTtlMinutes,
    @Valid Security security,
    @Valid Mail mail
) {

    public record Security(@NotBlank String signingSecret) {
    }

    public record Mail(
        String apiToken,
        @NotBlank String fromEmail,
        @NotBlank String fromName,
        boolean useSandbox,
        long sanboxInboxId
    ) {
    }
}
