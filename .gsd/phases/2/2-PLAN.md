---
phase: 2
plan: 2
wave: 2
gap_closure: false
---

# Plan 2.2: Service Layer Unit Tests (TagGenerator & YoutubeActivity)

## Objective
Establish pure unit tests with comprehensive assertions for `TagGeneratorService` and `YoutubeActivityService`, validating tag categorization, scoring thresholds, default parameter fallbacks, keyword extraction, and YouTube view estimation bounds.

## Context
Load these files for context:
- .gsd/SPEC.md
- .gsd/ARCHITECTURE.md
- src/main/java/com/technova/tagstudio/service/TagGeneratorService.java
- src/main/java/com/technova/tagstudio/service/YoutubeActivityService.java
- src/main/java/com/technova/tagstudio/dto/TagStudioRequestDTO.java
- src/main/java/com/technova/tagstudio/dto/TagStudioResponseDTO.java
- src/main/java/com/technova/tagstudio/dto/YoutubeActivityDTO.java

## Tasks

<task type="auto">
  <name>Create unit tests for TagGeneratorService</name>
  <files>
    src/test/java/com/technova/tagstudio/service/TagGeneratorServiceTest.java
  </files>
  <action>
    1. Create `TagGeneratorServiceTest` without Spring context dependency (pure JUnit 5 instantiation: `new TagGeneratorService()`).
    2. Test default parameters fallback when `videoTopic` and `channelName` are empty.
    3. Test prompt-based parsing (`The Topic: ...`, `Channel: ...`).
    4. Test tag generation and scoring: verify tags are sorted by score descending, non-empty, and contain expected categories (SEO, Performance, DevOps, etc.).
    5. Test edge cases: empty strings, null request fields, special characters in topic.
    
    AVOID: Spring Boot context spin-up when testing pure Java algorithmic logic.
    USE: Plain JUnit 5 `@Test`, `assertNotNull`, `assertFalse`, `assertTrue`.
  </action>
  <verify>
    Verify tests compile and pass: `TagGeneratorServiceTest`.
  </verify>
  <done>
    `TagGeneratorService` has thorough unit test coverage covering tag generation, category partitioning, prompt parsing, and score sorting.
  </done>
</task>

<task type="auto">
  <name>Create unit tests for YoutubeActivityService</name>
  <files>
    src/test/java/com/technova/tagstudio/service/YoutubeActivityServiceTest.java
  </files>
  <action>
    1. Create `YoutubeActivityServiceTest` (pure JUnit 5).
    2. Test handling of null or empty query: verifies error message returned in DTO.
    3. Test deterministic view calculation: identical queries produce identical results.
    4. Test multiplier heuristics: queries containing `"shorts"` or `"viral"` yield elevated view counts; queries containing `"tutorial"` apply the 1.5x multiplier.
    5. Test lower-bound constraint: verify `avgViews` is always >= 1200.
    
    AVOID: Relying on real YouTube network endpoints in unit tests.
    USE: Deterministic unit tests testing the simulation logic and boundary constraints.
  </action>
  <verify>
    Verify tests compile and pass: `YoutubeActivityServiceTest`.
  </verify>
  <done>
    `YoutubeActivityService` has unit tests verifying input validation, multiplier calculations, lower bounds, and determinism.
  </done>
</task>

## Must-Haves
After all tasks complete, verify:
- [ ] `TagGeneratorServiceTest` tests default generation, prompt extraction, categories, and null safety
- [ ] `YoutubeActivityServiceTest` tests null/empty handling, multiplier rules, and minimum view bounds

## Success Criteria
- [ ] Both test classes exist in `src/test/java/com/technova/tagstudio/service/`
- [ ] All tests run fast (<500ms) with zero external dependencies
