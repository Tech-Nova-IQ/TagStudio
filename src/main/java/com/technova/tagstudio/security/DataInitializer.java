package com.technova.tagstudio.security;

import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.AdminAuditLog;
import com.technova.tagstudio.model.Role;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.AdminAuditLogRepository;
import com.technova.tagstudio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.admin.name:TechNova Administrator}")
    private String adminName;

    @Value("${app.bootstrap.admin.email:admin@technovaiq.com}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:Admin@TechNova2026!}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           AdminAuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapEnabled) {
            logger.info("[BOOTSTRAP] Administrator bootstrap is disabled.");
            return;
        }

        long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
        if (adminCount > 0) {
            logger.info("[BOOTSTRAP] Administrator already exists (count={}). Skipping bootstrap.", adminCount);
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            logger.info("[BOOTSTRAP] Account with email {} already exists. Skipping bootstrap.", normalizedEmail);
            return;
        }

        // Initialize first administrator idempotently
        User admin = new User();
        admin.setFullName(adminName.trim());
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ROLE_ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setEmailVerified(true);

        User savedAdmin = userRepository.save(admin);
        logger.info("[BOOTSTRAP] Initialized primary administrator id={} email={} (Role: ROLE_ADMIN, Status: ACTIVE)",
                savedAdmin.getId(), savedAdmin.getEmail());

        // Audit log for bootstrap
        AdminAuditLog audit = new AdminAuditLog(
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                "SYSTEM_BOOTSTRAP_ADMIN",
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                "Primary administrator securely initialized via bootstrap configuration."
        );
        auditLogRepository.save(audit);
    }
}
