package com.auth.emails.springauthemails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringAuthEmailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAuthEmailsApplication.class, args);
    }

}
