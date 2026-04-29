package com.auth.emails.springauthemails.web;

import com.auth.emails.springauthemails.user.AppUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            return "redirect:/login";
        }

        return principal.isVerified() ? "redirect:/dashboard" : "redirect:/verify-email";
    }
}
