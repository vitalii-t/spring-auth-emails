package com.auth.emails.springauthemails.web;

import com.auth.emails.springauthemails.auth.VerificationService;
import com.auth.emails.springauthemails.user.AppUserPrincipal;
import com.auth.emails.springauthemails.user.User;
import com.auth.emails.springauthemails.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final UserRepository userRepository;

    @GetMapping("/verify-email")
    public String showVerificationNotice(Authentication authentication, Model model) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.isVerified()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("email", principal.getUsername());

        return "auth/verify-email";
    }

    @PostMapping("/verify-email/resend")
    public String resend(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        verificationService.sendVerificationEmail(user);
        redirectAttributes.addFlashAttribute("status", "Verification link sent.");

        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email/confirm")
    public String verify(
        @RequestParam("uid") long userId,
        @RequestParam("expires") long expires,
        @RequestParam("signature") String signature,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        if (!verificationService.verifyUser(userId, expires, signature)) {
            redirectAttributes.addFlashAttribute("error", "Verification link is invalid or expired.");
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElseThrow();
        if (authentication != null && authentication.getName().equalsIgnoreCase(user.getEmail())) {
            AppUserPrincipal principal = new AppUserPrincipal(user);
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    principal,
                    authentication.getCredentials(),
                    principal.getAuthorities()
                )
            );
            redirectAttributes.addFlashAttribute("status", "Email verified successfully. You can access the dashboard now.");
            return "redirect:/dashboard";
        }

        redirectAttributes.addFlashAttribute("status", "Email verified successfully. You can access the dashboard now.");

        return "redirect:/login";
    }
}
