package com.technova.tagstudio.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage;

        if (exception instanceof DisabledException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof LockedException) {
            errorMessage = "Your account has been locked. Please contact support.";
        } else if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
            errorMessage = "Invalid email address or password.";
        } else {
            errorMessage = "Authentication failed: " + exception.getMessage();
        }

        logger.warn("[AUTH] Authentication failure: {}", errorMessage);

        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect("/login?error=" + encodedError);
    }
}
