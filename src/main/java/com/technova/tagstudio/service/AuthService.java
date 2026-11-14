package com.technova.tagstudio.service;

import com.technova.tagstudio.dto.RegisterRequestDTO;
import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.EmailVerificationToken;
import com.technova.tagstudio.model.Role;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.UserRepository;
import com.technova.tagstudio.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.verification-token-expiry-hours:24}")
    private int tokenExpiryHours;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public User registerUser(RegisterRequestDTO dto) {
        if (!dto.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists");
        }

        // Always assign ROLE_USER and PENDING_VERIFICATION on public registration
        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        logger.info("[AUTH] Registered new user id={} email={} (Role: USER, Status: PENDING_VERIFICATION)",
                savedUser.getId(), savedUser.getEmail());

        // Generate and store verification token hash
        String plainToken = createAndSaveVerificationToken(savedUser);

        // Send confirmation email
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), plainToken);

        return savedUser;
    }

    @Transactional
    public VerificationResult verifyEmailToken(String plainToken) {
        if (plainToken == null || plainToken.trim().isEmpty()) {
            return new VerificationResult(false, "Verification token cannot be empty.", null);
        }

        String tokenHash = hashToken(plainToken.trim());
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            logger.warn("[AUTH] Verification failed: Token hash not found in database");
            return new VerificationResult(false, "Invalid verification link. Token not recognized or does not exist.", null);
        }

        EmailVerificationToken token = tokenOpt.get();

        if (token.isUsed()) {
            logger.warn("[AUTH] Verification failed: Token id={} has already been used at {}", token.getId(), token.getUsedAt());
            return new VerificationResult(false, "This verification link has already been used. Please log in.", token.getUser());
        }

        if (token.isExpired()) {
            logger.warn("[AUTH] Verification failed: Token id={} expired at {}", token.getId(), token.getExpiresAt());
            return new VerificationResult(false, "This verification link has expired. Please request a new verification email.", token.getUser());
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        logger.info("[AUTH] User id={} email={} verified successfully and marked ACTIVE", user.getId(), user.getEmail());
        return new VerificationResult(true, "Your email has been successfully verified! You may now log in.", user);
    }

    @Transactional
    public ResendResult resendVerificationEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ResendResult(false, "Please provide a valid email address.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalizedEmail);

        // Account enumeration protection: always return friendly message even if user does not exist
        if (userOpt.isEmpty()) {
            logger.info("[AUTH] Resend verification requested for non-existent email: {}", normalizedEmail);
            return new ResendResult(true, "If an unverified account exists with that email, a fresh verification link has been dispatched.");
        }

        User user = userOpt.get();

        if (user.isEmailVerified() && user.getAccountStatus() == AccountStatus.ACTIVE) {
            return new ResendResult(false, "Your email is already verified. You can log in directly.");
        }

        if (user.getAccountStatus() == AccountStatus.DISABLED || user.getAccountStatus() == AccountStatus.LOCKED) {
            return new ResendResult(false, "This account is currently " + user.getAccountStatus().name().toLowerCase() + ". Please contact an administrator.");
        }

        // Rate limiting check: check if a token was created in the last 2 minutes
        Optional<EmailVerificationToken> latestToken = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latestToken.isPresent() && latestToken.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(2))) {
            return new ResendResult(false, "Please wait at least 2 minutes between verification email requests.");
        }

        String plainToken = createAndSaveVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), plainToken);

        logger.info("[AUTH] New verification email dispatched for user id={} email={}", user.getId(), user.getEmail());
        return new ResendResult(true, "A fresh verification link has been sent to your email address. Please check your inbox.");
    }

    private String createAndSaveVerificationToken(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashToken(plainToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);

        EmailVerificationToken token = new EmailVerificationToken(user, tokenHash, expiresAt);
        tokenRepository.save(token);

        return plainToken;
    }

    public String hashToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public record VerificationResult(boolean success, String message, User user) {}
    public record ResendResult(boolean success, String message) {}
}
