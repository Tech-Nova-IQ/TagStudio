package com.technova.tagstudio.service;

import com.technova.tagstudio.dto.AdminUserViewDTO;
import com.technova.tagstudio.dto.DashboardStatsDTO;
import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.AdminAuditLog;
import com.technova.tagstudio.model.Role;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.AdminAuditLogRepository;
import com.technova.tagstudio.repository.UserRepository;
import com.technova.tagstudio.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository,
                        VerificationTokenRepository tokenRepository,
                        AdminAuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalUsers(userRepository.count());
        stats.setVerifiedUsers(userRepository.countByEmailVerified(true));
        stats.setPendingVerifications(userRepository.countByAccountStatus(AccountStatus.PENDING_VERIFICATION));
        stats.setActiveUsers(userRepository.countByAccountStatus(AccountStatus.ACTIVE));
        stats.setDisabledUsers(userRepository.countByAccountStatus(AccountStatus.DISABLED));
        stats.setLockedUsers(userRepository.countByAccountStatus(AccountStatus.LOCKED));
        stats.setAdminCount(userRepository.countByRole(Role.ROLE_ADMIN));

        List<AdminUserViewDTO> recent = userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .map(AdminUserViewDTO::fromUser)
                .collect(Collectors.toList());
        stats.setRecentUsers(recent);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<AdminUserViewDTO> searchUsers(String query, AccountStatus status) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return userRepository.searchUsers(cleanQuery, status)
                .stream()
                .map(AdminUserViewDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void activateUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        target.setAccountStatus(AccountStatus.ACTIVE);
        target.setEmailVerified(true);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "USER_ACTIVATE", target, "Manually activated user account and confirmed email verification.");
    }

    @Transactional
    public void disableUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        validateNotLastActiveAdmin(target, "Cannot disable the last active administrator.");

        target.setAccountStatus(AccountStatus.DISABLED);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "USER_DISABLE", target, "Disabled user account.");
    }

    @Transactional
    public void lockUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        validateNotLastActiveAdmin(target, "Cannot lock the last active administrator.");

        target.setAccountStatus(AccountStatus.LOCKED);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "USER_LOCK", target, "Locked user account.");
    }

    @Transactional
    public void unlockUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        target.setAccountStatus(AccountStatus.ACTIVE);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "USER_UNLOCK", target, "Unlocked user account and restored active status.");
    }

    @Transactional
    public void promoteToAdmin(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        if (target.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        target.setRole(Role.ROLE_ADMIN);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "ROLE_PROMOTE_ADMIN", target, "Promoted user to ROLE_ADMIN.");
    }

    @Transactional
    public void demoteToUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        if (target.getRole() != Role.ROLE_ADMIN) {
            return;
        }
        validateNotLastActiveAdmin(target, "Cannot demote the last active administrator.");

        target.setRole(Role.ROLE_USER);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);

        logAction(admin, "ROLE_DEMOTE_USER", target, "Demoted administrator to ROLE_USER.");
    }

    @Transactional
    public void deleteUser(Long targetUserId, User admin) {
        User target = getUserOrThrow(targetUserId);
        if (target.getRole() == Role.ROLE_ADMIN) {
            validateNotLastActiveAdmin(target, "Cannot delete the last active administrator.");
        }
        if (admin != null && admin.getId() != null && admin.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Administrators cannot delete their own account from the admin dashboard.");
        }

        // Delete associated tokens first
        tokenRepository.deleteByUser(target);

        // Record log before deleting user record
        logAction(admin, "USER_DELETE", target, "Permanently deleted user account " + target.getEmail());

        userRepository.delete(target);
        logger.info("[ADMIN] Administrator id={} deleted user id={} email={}",
                admin != null ? admin.getId() : 0, targetUserId, target.getEmail());
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    private User getUserOrThrow(Long userId) {
        Long safeId = Objects.requireNonNull(userId, "User ID cannot be null");
        return userRepository.findById(safeId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " does not exist."));
    }

    private void validateNotLastActiveAdmin(User target, String errorMessage) {
        if (target.getRole() == Role.ROLE_ADMIN && target.getAccountStatus() == AccountStatus.ACTIVE) {
            long activeAdminCount = userRepository.countByRoleAndAccountStatus(Role.ROLE_ADMIN, AccountStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException(errorMessage);
            }
        }
    }

    private void logAction(User admin, String action, User target, String details) {
        Long adminId = (admin != null && admin.getId() != null) ? admin.getId() : 0L;
        String adminEmail = (admin != null && admin.getEmail() != null) ? admin.getEmail() : "system";

        AdminAuditLog log = new AdminAuditLog(
                adminId,
                adminEmail,
                action,
                target != null ? target.getId() : null,
                target != null ? target.getEmail() : null,
                details
        );
        auditLogRepository.save(log);
        logger.info("[AUDIT_LOG] admin={} action={} target={} details={}", adminEmail, action,
                target != null ? target.getEmail() : "N/A", details);
    }
}
