package com.auth.emails.springauthemails.config;

import com.auth.emails.springauthemails.auth.VerifiedUserAuthorizationManager;
import com.auth.emails.springauthemails.user.AppUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        VerifiedUserAuthorizationManager verifiedUserAuthorizationManager,
        AppUserDetailsService userDetailsService
    ) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/",
                    "/register",
                    "/login",
                    "/forgot-password",
                    "/reset-password",
                    "/verify-email/confirm",
                    "/css/**",
                    "/h2-console/**"
                ).permitAll()
                .requestMatchers("/verify-email", "/verify-email/resend", "/logout").authenticated()
                .requestMatchers("/dashboard").access(verifiedUserAuthorizationManager)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(this::handleAccessDenied)
            )
            .requestCache(cache -> cache.requestCache(new HttpSessionRequestCache()))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean verified = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VERIFIED"));

            response.sendRedirect(verified ? "/dashboard" : "/verify-email");
        };
    }

    private void handleAccessDenied(
        HttpServletRequest request,
        HttpServletResponse response,
        org.springframework.security.access.AccessDeniedException exception
    ) throws IOException, ServletException {
        Authentication authentication = (Authentication) request.getUserPrincipal();
        if (authentication != null && authentication.isAuthenticated()) {
            response.sendRedirect("/verify-email");
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
