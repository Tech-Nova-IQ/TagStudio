package com.technova.tagstudio.security;

import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("[AUTH] Login attempt for non-existent email: {}", normalizedEmail);
                    return new UsernameNotFoundException("Invalid email address or password.");
                });

        // Enforce account status restrictions before authentication
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            logger.warn("[AUTH] Login rejected: User {} is PENDING_VERIFICATION", normalizedEmail);
            throw new DisabledException("Your email has not been verified yet. Please check your inbox or request a new verification link.");
        }

        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            logger.warn("[AUTH] Login rejected: User {} is DISABLED", normalizedEmail);
            throw new DisabledException("Your account has been disabled. Please contact an administrator.");
        }

        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            logger.warn("[AUTH] Login rejected: User {} is LOCKED", normalizedEmail);
            throw new LockedException("Your account has been locked. Please contact support.");
        }

        boolean enabled = (user.getAccountStatus() == AccountStatus.ACTIVE);
        boolean accountNonLocked = (user.getAccountStatus() != AccountStatus.LOCKED);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                enabled,
                true,
                true,
                accountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
