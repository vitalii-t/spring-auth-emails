package com.auth.emails.springauthemails.web;

import com.auth.emails.springauthemails.auth.RegistrationService;
import com.auth.emails.springauthemails.user.AppUserPrincipal;
import com.auth.emails.springauthemails.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("form", new AuthForms.RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("form") AuthForms.RegisterForm form,
        BindingResult bindingResult,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        if (!form.password().equals(form.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            User user = registrationService.register(form.name(), form.email(), form.password());
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    new AppUserPrincipal(user),
                    user.getPasswordHash(),
                    new AppUserPrincipal(user).getAuthorities()
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );
            return "redirect:/verify-email";
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }
}
