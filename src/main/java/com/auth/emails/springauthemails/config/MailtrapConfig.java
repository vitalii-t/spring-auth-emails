package com.auth.emails.springauthemails.config;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig.Builder;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailtrapConfig {

    @Bean
    MailtrapClient mailtrapClient(AppProperties properties) {
        final Builder builder = new Builder()
            .token(properties.mail().apiToken());

        if (properties.mail().useSandbox()) {
            builder
                .sandbox(true)
                .inboxId(properties.mail().sanboxInboxId());
        }

        return
            MailtrapClientFactory.createMailtrapClient(
                builder
                    .build()
            );
    }
}
