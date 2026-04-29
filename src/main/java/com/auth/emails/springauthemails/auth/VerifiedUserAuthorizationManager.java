package com.auth.emails.springauthemails.auth;

import com.auth.emails.springauthemails.user.AppUserPrincipal;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Component
public class VerifiedUserAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(
        Supplier<Authentication> authenticationSupplier,
        RequestAuthorizationContext context
    ) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            return new AuthorizationDecision(false);
        }

        return new AuthorizationDecision(principal.isVerified());
    }
}
