package com.auth.emails.springauthemails.web;

import com.auth.emails.springauthemails.auth.PasswordResetService;
import com.auth.emails.springauthemails.auth.PasswordResetService.PasswordResetResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("form", new AuthForms.ForgotPasswordForm());

        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetLink(
        @Valid @ModelAttribute("form") AuthForms.ForgotPasswordForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        passwordResetService.sendResetLink(form.email());
        redirectAttributes.addFlashAttribute("status", "If that email is registered, we've sent a reset link.");

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(
        @RequestParam("token") String token,
        @RequestParam("email") String email,
        Model model
    ) {
        model.addAttribute("form", new AuthForms.ResetPasswordForm(token, email, "", ""));

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
        @Valid @ModelAttribute("form") AuthForms.ResetPasswordForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (!form.password().equals(form.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        PasswordResetResult result = passwordResetService.resetPassword(
            form.email(),
            form.token(),
            form.password()
        );

        if (result != PasswordResetResult.SUCCESS) {
            bindingResult.reject("invalid", "Reset link is invalid or expired.");
            return "auth/reset-password";
        }

        redirectAttributes.addFlashAttribute("status", "Password updated successfully. Please sign in.");

        return "redirect:/login";
    }
}
