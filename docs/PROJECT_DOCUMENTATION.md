# TechNova Tag Studio — Comprehensive System Documentation

**Project:** TechNova Tag Studio  
**Version:** 1.5.0  
**Repository:** [https://github.com/Tech-Nova-IQ/TagStudio.git](https://github.com/Tech-Nova-IQ/TagStudio.git)  
**Branch:** `main`  
**Runtime:** Java 21 LTS | Spring Boot 3.3.5 | Spring Security 6 | Thymeleaf | Google Gemini AI (`gemini-3.8-flash`)

---

## 1. System Access & Default Credentials

When the application boots up, the idempotent `DataInitializer` checks if an administrator exists. If none is found, it securely creates the default administrator account.

### 🔑 Administrator Account (Bootstrap)

| Field | Value |
|-------|-------|
| **Role** | `ROLE_ADMIN` |
| **Email** | `admin@technovaiq.com` |
| **Password** | `Admin@TechNova2026!` |
| **Account Status** | `ACTIVE` |
| **Email Verified** | `Yes (true)` |
| **Access Rights** | Full administrative console (`/admin/**`), user management, role assignment, audit logs, and Tag Studio console |

> [!NOTE]
> The default bootstrap credentials can be customized via environment variables or in `application.properties`:
> - `app.bootstrap.admin.email`
> - `app.bootstrap.admin.password`
> - `app.bootstrap.admin.name`
> - `app.bootstrap.admin.enabled`

---

### 👤 Standard User Account (Self-Registration)

| Field | Value |
|-------|-------|
| **Registration URL** | `http://localhost:8080/register` |
| **Role** | `ROLE_USER` (Public registration cannot assign admin privileges) |
| **Initial Status** | `PENDING_VERIFICATION` |
| **Verification** | A verification link is sent to the registered email address (also logged to server output in local development). Once clicked, status becomes `ACTIVE`. |
| **Access Rights** | Tag Studio Console (`/`), User Dashboard (`/dashboard`), Shorts/Video/Playlist generation, Studio AI chat |

---

## 2. Key URLs & Endpoints

| Endpoint | Access Level | Description |
|----------|--------------|-------------|
| `/` | `ROLE_USER`, `ROLE_ADMIN` | Main TechNova Tag Studio Application |
| `/login` | Public | Authentication page (Email & Password) |
| `/register` | Public | User Registration (Full Name, Email, Password, Terms) |
| `/verify-email?token=...` | Public | Secure token verification endpoint |
| `/resend-verification` | Public | Rate-limited token resend request page |
| `/dashboard` | `ROLE_USER`, `ROLE_ADMIN` | Personal User Dashboard (Profile, Status, Quick Actions) |
| `/admin/dashboard` | `ROLE_ADMIN` Only | Dedicated Administrator Dashboard & User Management |
| `/admin/users/**` | `ROLE_ADMIN` Only | Admin actions: activate, disable, lock, promote, demote, delete |
| `/logout` | Authenticated | Secure session invalidation and cookie cleanup |
| `/access-denied` | Public | Custom 403 Access Denied page |

---

## 3. Executive Summary of Work Completed (Phases 1 – 5)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               TECHNOVA TAG STUDIO                                      │
├────────────────────────────┬─────────────────────────────┬─────────────────────────────┤
│   Phase 1 & Phase 2        │   Phase 3 & Phase 4         │   Phase 5                   │
│   AI & Resilient Core      │   Formatting & SEO Quality  │   Security & Admin SaaS     │
├────────────────────────────┼─────────────────────────────┼─────────────────────────────┤
│ • Clean Title Formatting   │ • YouTube Shorts '\u200B'   │ • Spring Security 6 Auth    │
│ • Mandatory CTAs           │   paragraph preservation    │ • Email Verification Hash   │
│ • UI Category Context      │ • Stripped bold asterisks   │ • ROLE_ADMIN & ROLE_USER    │
│ • Gemini HTTP Error Map    │ • 9-Part ChatGPT SEO format │ • Dedicated Admin Dashboard │
│ • 503 Exponential Backoff  │ • TagGeneratorService.java  │ • Last-Admin Safeguards     │
│ • Zero Credential Leaks    │   strictly frozen & intact  │ • Admin Action Audit Logs   │
└────────────────────────────┴─────────────────────────────┴─────────────────────────────┘
```

---

### Phase 1: Studio AI Titles, CTA & Context Integration
- **Objective:** Eliminate noise in Studio AI output, ensure Call to Action compliance, and link active UI categories to the AI context.
- **Key Deliverables:**
  - Removed unwanted prefixes (`"The Topic: **..."`, stray asterisks) from generated titles.
  - Implemented mandatory CTA injection whenever descriptions or metadata are generated.
  - Passed selected video topics and active category checkboxes directly from the frontend to Studio AI.

---

### Phase 2: Studio AI Gemini Error Diagnosis & Resilient Handling
- **Objective:** Robust error categorisation and resilient backoff for Google Gemini API integration.
- **Key Deliverables:**
  - Distinct categorization and logging for HTTP error codes: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `429 Rate Limit`, and `503 Service Unavailable`.
  - Implemented jittered exponential backoff retry for transient `503` spikes.
  - Strict log sanitization: masked API keys and tokens from appearing in console/file logs.

---

### Phase 3: YouTube Description Copy Formatting & Shorts Compatibility
- **Objective:** Fix formatting collapses when copying descriptions into YouTube Shorts.
- **Key Deliverables:**
  - **Zero-Width Space Preservation:** Automatically populated blank lines with `\u200B` characters so YouTube Shorts description textboxes do not collapse paragraphs into a single wall of text.
  - **Asterisk & Entity Stripping:** Stripped raw markdown bold asterisks (`**bold**` → `bold`) and decoded HTML entities while keeping bullet points and emojis intact.
  - **Chat Copy Button:** Added a dedicated copy button directly in Studio AI chat message headers.

---

### Phase 4: YouTube Description SEO Quality Enhancement
- **Objective:** Upgrade metadata descriptions to a professional ChatGPT structure with high-intent SEO.
- **Key Deliverables:**
  - Implemented the **9-Part ChatGPT Description Structure**:
    1. Hook
    2. Topic Explanation
    3. What You'll Learn (Core concept bullets)
    4. Key Takeaway
    5. Target Audience
    6. Engagement CTA
    7. Like & Subscribe CTA
    8. Brand Closing Line
    9. Concise Hashtags (5–8 targeted tags)
  - Replaced repetitive keyword dumps with organic, conceptual bullet points.
  - **Protected File Rule:** `TagGeneratorService.java` remained completely untouched (0 lines changed); curation handled cleanly in `ChatService.java` and `app.js`.

---

### Phase 5: Secure Authentication, Email Verification & Role-Based Admin Panel
- **Objective:** Deliver a production-ready authentication and authorization system.
- **Key Deliverables:**
  - **Data Model:** Created JPA entities `User`, `EmailVerificationToken`, and `AdminAuditLog` with Spring Data JPA repositories.
  - **Password Security:** BCrypt password hashing via Spring Security `PasswordEncoder`.
  - **Email Verification Workflow:** Single-use tokens stored exclusively as SHA-256 hashes, with expiration, rate-limited resend, and account activation.
  - **Role-Based Access Control:** Strict URL matching and method security isolating `ROLE_ADMIN` from `ROLE_USER`.
  - **Admin Console (`/admin/dashboard`):** Real-time KPI statistics (Total, Verified, Pending, Active, Disabled, Locked), user search, status filtering, account actions, and role promotion/demotion.
  - **Last Administrator Safeguards:** Backend checks guarantee the system can never delete, demote, lock, or disable the final active administrator.
  - **Audit Logging:** Every administrative action is persistently recorded in `admin_audit_logs`.
  - **Automated Verification:** 25/25 automated tests green (`AuthAndAdminIntegrationTest` + `ChatServiceTest`).

---

## 4. Git & CodeRabbit Integration

- **Remote Repository:** `https://github.com/Tech-Nova-IQ/TagStudio.git` (`main` branch)
- **Local Working Copy:** `G:\technova-tag-studio`
- **Backup / Secondary Mirror:** `G:\GitHub\technova-tag-studio`
- **CodeRabbit Synchronization (`.coderabbit.yaml`):**
  - Configured repository-level `.coderabbit.yaml` instructing CodeRabbit to review PRs against GSD specifications (`.gsd/SPEC.md`, `.gsd/ROADMAP.md`, `PROJECT_RULES.md`).
  - Strict guardrail in place flagging any modifications to protected file `TagGeneratorService.java`.
  - Configured `.vscode/settings.json` with `"git.path": "C:\\Program Files\\Git\\cmd\\git.exe"` to resolve CodeRabbit VS Code Git model initialization.
