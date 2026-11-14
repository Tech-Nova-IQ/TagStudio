package com.technova.tagstudio;

import com.technova.tagstudio.dto.RegisterRequestDTO;
import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.EmailVerificationToken;
import com.technova.tagstudio.model.Role;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.AdminAuditLogRepository;
import com.technova.tagstudio.repository.UserRepository;
import com.technova.tagstudio.repository.VerificationTokenRepository;
import com.technova.tagstudio.service.AdminService;
import com.technova.tagstudio.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional
    public void setup() {
        // Clean up test non-admin users
        userRepository.findAll().forEach(u -> {
            if (!"admin@technovaiq.com".equalsIgnoreCase(u.getEmail())) {
                tokenRepository.deleteByUser(u);
                userRepository.delete(u);
            }
        });
    }

    @Test
    public void testFirstAdministratorBootstrap() {
        Optional<User> adminOpt = userRepository.findByEmailIgnoreCase("admin@technovaiq.com");
        assertTrue(adminOpt.isPresent(), "Bootstrap admin must exist");
        User admin = adminOpt.get();
        assertEquals(Role.ROLE_ADMIN, admin.getRole(), "Bootstrap user must have ROLE_ADMIN");
        assertEquals(AccountStatus.ACTIVE, admin.getAccountStatus(), "Bootstrap user must be ACTIVE");
        assertTrue(admin.isEmailVerified(), "Bootstrap user must be email verified");
        assertTrue(passwordEncoder.matches("Admin@TechNova2026!", admin.getPasswordHash()), "Bootstrap password must match");
    }

    @Test
    public void testUserRegistrationAndPendingVerificationStatus() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFullName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPassword("Secret@1234");
        dto.setConfirmPassword("Secret@1234");
        dto.setAgreeTerms(true);

        User registered = authService.registerUser(dto);

        assertNotNull(registered.getId());
        assertEquals(Role.ROLE_USER, registered.getRole(), "New users must receive ROLE_USER");
        assertEquals(AccountStatus.PENDING_VERIFICATION, registered.getAccountStatus(), "New users must be PENDING_VERIFICATION");
        assertFalse(registered.isEmailVerified(), "Email must not be verified initially");

        // Verify token was stored hashed
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findTopByUserOrderByCreatedAtDesc(registered);
        assertTrue(tokenOpt.isPresent(), "Verification token record must be created in DB");
        EmailVerificationToken token = tokenOpt.get();
        assertNotNull(token.getTokenHash());
        assertFalse(token.getTokenHash().isEmpty());
        assertNull(token.getUsedAt(), "Token must not be used yet");
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()), "Token expiration must be in the future");
    }

    @Test
    public void testEmailVerificationWorkflow() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFullName("Alice Smith");
        dto.setEmail("alice.smith@example.com");
        dto.setPassword("AlicePass@2026");
        dto.setConfirmPassword("AlicePass@2026");
        dto.setAgreeTerms(true);

        User user = authService.registerUser(dto);
        EmailVerificationToken token = tokenRepository.findTopByUserOrderByCreatedAtDesc(user).orElseThrow();

        // 1. Simulate invalid token
        AuthService.VerificationResult invalidResult = authService.verifyEmailToken("completely-invalid-token");
        assertFalse(invalidResult.success(), "Invalid token must be rejected");

        // 2. We need the plaintext token to test successful verification.
        // Let's test by generating a known token
        String testPlainToken = "test-plain-token-alice-12345";
        String testHash = authService.hashToken(testPlainToken);
        token.setTokenHash(testHash);
        tokenRepository.save(token);

        AuthService.VerificationResult successResult = authService.verifyEmailToken(testPlainToken);
        assertTrue(successResult.success(), "Valid token must succeed");

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.isEmailVerified(), "User emailVerified must be true");
        assertEquals(AccountStatus.ACTIVE, updatedUser.getAccountStatus(), "User accountStatus must be ACTIVE");

        // 3. Test token reuse prevention (single use)
        AuthService.VerificationResult reuseResult = authService.verifyEmailToken(testPlainToken);
        assertFalse(reuseResult.success(), "Previously used token must be rejected");
        assertTrue(reuseResult.message().contains("already been used"));
    }

    @Test
    public void testExpiredVerificationTokenRejection() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFullName("Bob Marley");
        dto.setEmail("bob.marley@example.com");
        dto.setPassword("BobPassword@99");
        dto.setConfirmPassword("BobPassword@99");
        dto.setAgreeTerms(true);

        User user = authService.registerUser(dto);
        EmailVerificationToken token = tokenRepository.findTopByUserOrderByCreatedAtDesc(user).orElseThrow();

        String expiredPlainToken = "expired-token-bob-999";
        token.setTokenHash(authService.hashToken(expiredPlainToken));
        token.setExpiresAt(LocalDateTime.now().minusHours(2)); // Expired 2 hours ago
        tokenRepository.save(token);

        AuthService.VerificationResult expiredResult = authService.verifyEmailToken(expiredPlainToken);
        assertFalse(expiredResult.success(), "Expired token must be rejected");
        assertTrue(expiredResult.message().contains("expired"));
    }

    @Test
    public void testRoleBasedAccessControlMockMvc() throws Exception {
        // 1. Unauthenticated request to /dashboard redirects to /login
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // 2. Unauthenticated request to /admin/dashboard redirects to /login
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // 3. Public endpoints accessible without authentication
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "regular@example.com", authorities = {"ROLE_USER"})
    public void testUserCannotAccessAdminEndpoints() throws Exception {
        // Normal user with ROLE_USER must be denied access to /admin/** and redirected to /access-denied
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        mockMvc.perform(post("/admin/users/1/activate").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "admin@technovaiq.com", authorities = {"ROLE_ADMIN"})
    public void testAdminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    public void testAdminCannotDemoteOrDeleteLastActiveAdministrator() {
        User admin = userRepository.findByEmailIgnoreCase("admin@technovaiq.com").orElseThrow();

        // Attempting to demote the only admin must throw IllegalStateException
        IllegalStateException demoteEx = assertThrows(IllegalStateException.class, () -> {
            adminService.demoteToUser(admin.getId(), admin);
        });
        assertTrue(demoteEx.getMessage().contains("last active administrator"));

        // Attempting to delete the only admin must throw exception
        IllegalStateException deleteEx = assertThrows(IllegalStateException.class, () -> {
            adminService.deleteUser(admin.getId(), admin);
        });
        assertTrue(deleteEx.getMessage().contains("last active administrator"));
    }

    @Test
    public void testAdminUserManagementWorkflowWithAuditLog() {
        // Create user
        User targetUser = new User("Target User", "target@example.com", "passHash", Role.ROLE_USER, AccountStatus.PENDING_VERIFICATION);
        targetUser = userRepository.save(targetUser);

        User admin = userRepository.findByEmailIgnoreCase("admin@technovaiq.com").orElseThrow();

        // 1. Activate
        adminService.activateUser(targetUser.getId(), admin);
        User activated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, activated.getAccountStatus());
        assertTrue(activated.isEmailVerified());

        // 2. Lock
        adminService.lockUser(targetUser.getId(), admin);
        User locked = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(AccountStatus.LOCKED, locked.getAccountStatus());

        // 3. Unlock
        adminService.unlockUser(targetUser.getId(), admin);
        User unlocked = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, unlocked.getAccountStatus());

        // 4. Promote
        adminService.promoteToAdmin(targetUser.getId(), admin);
        User promoted = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.ROLE_ADMIN, promoted.getRole());

        // 5. Demote
        adminService.demoteToUser(targetUser.getId(), admin);
        User demoted = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.ROLE_USER, demoted.getRole());

        // Verify audit logs exist
        assertTrue(auditLogRepository.count() >= 5, "Audit logs must record all administrative actions");
    }
}
