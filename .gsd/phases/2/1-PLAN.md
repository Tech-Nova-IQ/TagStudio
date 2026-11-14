---
phase: 2
plan: 1
wave: 1
gap_closure: false
---

# Plan 2.1: Studio AI Gemini Error Diagnosis, Status Code Categorization & Resilient Handling

## Objective
Diagnose and resolve the Studio AI Gemini failure observed in application logs (`HTTP 503 UNAVAILABLE: This model is currently experiencing high demand`). Implement structured server-side diagnostic logging, accurate categorization across all HTTP status codes (400, 401, 403, 404, 429, 503, 5xx), and a resilient exponential backoff retry mechanism for transient server-side spikes while preserving all completed Phase 1 functionality (clean titles, mandatory CTA, category context).

## Context
Load these files for context:
- .gsd/SPEC.md
- .gsd/ROADMAP.md
- src/main/java/com/technova/tagstudio/service/ChatService.java
- src/main/resources/application.properties
- src/test/java/com/technova/tagstudio/ChatServiceTest.java

## Tasks

<task type="auto">
  <name>Implement structured diagnostic logging and accurate HTTP error categorization</name>
  <files>
    src/main/java/com/technova/tagstudio/service/ChatService.java
  </files>
  <action>
    1. In `ChatService.java`:
       - Enhance `callGeminiWithRetry()` to handle all standard HTTP status codes distinctly:
         - **401 / 403 (Authentication/Permissions)**: Log `[GEMINI_AUTH_ERROR] HTTP {status}: Invalid API key or permission denied.` (never log the API key). Return user-facing: `"Studio AI: Authentication failed. Please check your GEMINI_API_KEY."`.
         - **404 (Model or Endpoint Not Found)**: Log `[GEMINI_NOT_FOUND] HTTP 404: Model or endpoint not found. Model: '{model}', URL: '{apiUrl}'. Error: {message}`. Return user-facing: `"Studio AI: Model or endpoint not found (" + model + "). Please verify configuration."`.
         - **429 (Quota vs Rate-Limit)**: Maintain distinction between `[GEMINI_QUOTA_EXHAUSTED]` (insufficient quota / credit balance) and `[GEMINI_RATE_LIMIT]` (transient RPM/TPM limit).
         - **503 / 5xx (Service Unavailable / Capacity Spike)**: Log `[GEMINI_SERVICE_UNAVAILABLE] HTTP {status} [{type}]: {message}`. Include attempt number and backoff duration in logs.
         - **400 (Bad Request / Invalid Payload)**: Log `[GEMINI_BAD_REQUEST] HTTP 400: {message}`. Return user-facing: `"Studio AI: Invalid request parameters (" + errorInfo.message + ")"`.
         - **Fallback (Unexpected Status Codes)**: Log `[GEMINI_HTTP_ERROR] HTTP {status}: {message}`.
       - Ensure `parseError()` captures `error`, `error.message`, `error.type`, and `error.code` robustly.
       - Never log the API key or raw Authorization headers.
    
    AVOID: Masking 404, 400, or 401 errors as "high demand".
    AVOID: Logging sensitive credentials.
    USE: Categorized log tags (`[GEMINI_...]`) for instant debugging from server console logs.
  </action>
  <verify>
    Verify compilation of `ChatService.java` with complete error code branches.
  </verify>
  <done>
    Server logs accurately categorize and log real Gemini API responses for 400, 401, 403, 404, 429, 503, and 5xx with clear diagnostic tags.
  </done>
</task>

<task type="auto">
  <name>Implement resilient exponential backoff retry and unit test suite</name>
  <files>
    src/main/java/com/technova/tagstudio/service/ChatService.java
    src/test/java/com/technova/tagstudio/ChatServiceTest.java
  </files>
  <action>
    1. In `ChatService.java`:
       - For transient errors (`503` and `429` rate-limit), implement multi-attempt backoff with jitter:
         - Attempt 1: backoff ~1500-2000ms.
         - Attempt 2: backoff ~3000-4000ms (max 2 retries).
       - Never retry non-transient errors (400, 401, 403, 404, or quota exhaustion).
       - If all retries for 503 are exhausted, return a clear, informative message:
         `"Studio AI: Google Gemini is currently experiencing high demand (HTTP 503: " + model + "). Please try again in a moment."`
    2. In `src/test/java/com/technova/tagstudio/ChatServiceTest.java`:
       - Add unit tests verifying:
         - 404 error response parsing and distinct error categorization.
         - 401/403 authentication error categorization.
         - 400 bad request error categorization.
         - 503 capacity error categorization.
       - Verify that all completed Phase 1 tests (`testStudioAiCleanTopicAndSanitizeReply`, `testStudioAiRequestBodyWithCategoriesCleanTitleAndCta`) continue to pass without alteration.
    
    AVOID: Replacing the Gemini model or swapping out Google Gemini.
    AVOID: Breaking existing Phase 1 title cleaning, CTA, or category context behaviors.
    USE: Deterministic unit tests for error categorization and prompt structure.
  </action>
  <verify>
    Run `mvnw test -Dtest=ChatServiceTest` to ensure all tests pass with zero errors.
  </verify>
  <done>
    `ChatService` handles transient Gemini 503 spikes with resilient backoff, logs clear diagnostics, and is verified by automated tests.
  </done>
</task>

## Must-Haves
After all tasks complete, verify:
- [ ] Server logs clearly report the underlying Gemini HTTP status code and message with `[GEMINI_...]` prefixes
- [ ] 404 status codes produce explicit "model or endpoint not found" diagnostic logs and messages (not generic 503 high demand)
- [ ] 401/403 status codes produce authentication failure diagnostic logs
- [ ] 503 transient demand spikes perform exponential backoff retries with full diagnostic logging
- [ ] API keys are never exposed in log outputs
- [ ] Phase 1 behavior is 100% preserved (clean titles without `"The Topic: **..."`, mandatory CTA, active category context)
- [ ] Google Gemini integration and configured model (`gemini-3.8-flash`) are preserved

## Success Criteria
- [ ] Zero UI redesign
- [ ] Zero changes to `TagGeneratorService.java` or `hashtag_generator.html`
- [ ] All unit tests pass cleanly via `mvnw test -Dtest=ChatServiceTest`
