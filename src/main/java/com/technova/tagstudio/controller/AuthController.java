package com.technova.tagstudio.controller;

import com.technova.tagstudio.dto.RegisterRequestDTO;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterRequestDTO());
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDTO") RegisterRequestDTO registerDTO,
                                 BindingResult bindingResult,
                                 Model model) {
        if (!registerDTO.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.registerDTO", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User user = authService.registerUser(registerDTO);
            return "redirect:/verify-email?registered=true&email=" + user.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        } catch (Exception e) {
            logger.error("[AUTH] Unexpected registration error", e);
            model.addAttribute("errorMessage", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/verify-email")
    public String showVerifyEmail(@RequestParam(value = "token", required = false) String token,
                                  @RequestParam(value = "registered", required = false) Boolean registered,
                                  @RequestParam(value = "email", required = false) String email,
                                  Model model) {
        if (token != null && !token.trim().isEmpty()) {
            AuthService.VerificationResult result = authService.verifyEmailToken(token);
            model.addAttribute("verified", result.success());
            model.addAttribute("message", result.message());
            if (result.user() != null) {
                model.addAttribute("email", result.user().getEmail());
            }
        } else if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("registered", true);
            model.addAttribute("email", email);
            model.addAttribute("message", "Registration successful! We have dispatched a confirmation email to " +
                    (email != null ? email : "your inbox") + ". Please verify your email before logging in.");
        } else {
            model.addAttribute("message", "Please click the verification link in your confirmation email to activate your account.");
        }
        return "verify-email";
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm(Model model) {
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String handleResendVerification(@RequestParam("email") String email, Model model) {
        AuthService.ResendResult result = authService.resendVerificationEmail(email);
        if (result.success()) {
            model.addAttribute("successMessage", result.message());
        } else {
            model.addAttribute("errorMessage", result.message());
        }
        model.addAttribute("email", email);
        return "resend-verification";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        model.addAttribute("successMessage", "If an active account exists for " + email + ", password reset instructions have been dispatched.");
        return "forgot-password";
    }

    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied";
    }
}
