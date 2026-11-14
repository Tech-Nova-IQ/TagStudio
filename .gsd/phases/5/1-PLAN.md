---
phase: 5
plan: 1
wave: 1
depends_on: []
files_modified:
  - pom.xml
  - src/main/resources/application.properties
  - src/main/java/com/technova/tagstudio/model/Role.java
  - src/main/java/com/technova/tagstudio/model/AccountStatus.java
  - src/main/java/com/technova/tagstudio/model/User.java
  - src/main/java/com/technova/tagstudio/model/EmailVerificationToken.java
  - src/main/java/com/technova/tagstudio/model/AdminAuditLog.java
  - src/main/java/com/technova/tagstudio/repository/UserRepository.java
  - src/main/java/com/technova/tagstudio/repository/VerificationTokenRepository.java
  - src/main/java/com/technova/tagstudio/repository/AdminAuditLogRepository.java
  - src/main/java/com/technova/tagstudio/security/SecurityConfig.java
  - src/main/java/com/technova/tagstudio/security/CustomUserDetailsService.java
  - src/main/java/com/technova/tagstudio/security/CustomAuthenticationSuccessHandler.java
  - src/main/java/com/technova/tagstudio/security/CustomAuthenticationFailureHandler.java
  - src/main/java/com/technova/tagstudio/security/CustomAccessDeniedHandler.java
  - src/main/java/com/technova/tagstudio/security/DataInitializer.java
  - src/main/java/com/technova/tagstudio/service/EmailService.java
  - src/main/java/com/technova/tagstudio/service/AuthService.java
  - src/main/java/com/technova/tagstudio/service/AdminService.java
  - src/main/java/com/technova/tagstudio/controller/AuthController.java
  - src/main/java/com/technova/tagstudio/controller/UserDashboardController.java
  - src/main/java/com/technova/tagstudio/controller/AdminController.java
  - src/main/resources/templates/register.html
  - src/main/resources/templates/login.html
  - src/main/resources/templates/verify-email.html
  - src/main/resources/templates/resend-verification.html
  - src/main/resources/templates/forgot-password.html
  - src/main/resources/templates/dashboard.html
  - src/main/resources/templates/admin/dashboard.html
  - src/main/resources/templates/access-denied.html
  - src/main/resources/templates/index.html
  - src/test/java/com/technova/tagstudio/AuthAndAdminIntegrationTest.java
autonomous: true
user_setup: []

must_haves:
  truths:
    - "New users register via email, password, and terms acceptance and receive ROLE_USER and PENDING_VERIFICATION"
    - "Verification tokens are stored as SHA-256 hashes and expire after a configurable duration"
    - "Clicking verification link marks email verified and transitions user to ACTIVE"
    - "Expired and reused verification tokens are rejected"
    - "Only ACTIVE users can log in"
    - "ROLE_ADMIN has exclusive access to the Admin Dashboard (/admin/dashboard)"
    - "Normal users are denied access to /admin/** and redirected to /access-denied"
    - "Admin dashboard provides real-time statistics, user search, filtering, activation, locking, promotion, demotion, and deletion"
    - "Last active administrator cannot be demoted, disabled, locked, or deleted"
    - "All administrative operations are recorded in the admin audit log"
    - "Primary administrator is securely and idempotently bootstrapped"
    - "Existing Tag Studio features and TagGeneratorService.java are preserved 100% untouched"
---

# Plan 5.1: Production-Ready SaaS Authentication, Email Verification & Admin Panel

<objective>
Implement a complete, secure authentication, email verification, and role-based administration system for TechNova Tag Studio without modifying TagGeneratorService.java or breaking existing application capabilities.
</objective>

<tasks>
<task type="auto">
  <name>Database persistence, models, repositories and DTOs</name>
  <action>Add Spring Security, Spring Data JPA, H2, and JavaMail dependencies. Create User, EmailVerificationToken, and AdminAuditLog entities and repositories.</action>
</task>

<task type="auto">
  <name>Security configuration and service layer</name>
  <action>Implement Spring Security 6 SecurityFilterChain, BCrypt password hashing, CustomUserDetailsService, EmailService, AuthService, and AdminService.</action>
</task>

<task type="auto">
  <name>Controllers and Thymeleaf UI views</name>
  <action>Create AuthController, UserDashboardController, AdminController, and all associated responsive HTML templates consistent with Tag Studio branding.</action>
</task>

<task type="auto">
  <name>Testing and empirical verification</name>
  <action>Create AuthAndAdminIntegrationTest and execute full test suite confirming 100% pass rate and regression safety.</action>
</task>
</tasks>
