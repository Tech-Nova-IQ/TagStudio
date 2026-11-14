# SPEC.md — Project Specification

> **Status**: `ACTIVE`
>
> **Project**: `OPEN` (Phase 3 Completed; Future Development Enabled)

## Vision

Enhance and fix the **Studio AI** assistant in **TechNova Tag Studio** so that it delivers clean, professional YouTube metadata and answers directly aligned with the user's active workspace context: clean titles without unwanted "The Topic: **...**" prefixes or markdown bolding, reliable inclusion of high-converting Calls to Action (CTA), and full contextual awareness of both the current video topic and the user's selected categories.

## Goals

1. **Clean Title Output** — Ensure Studio AI outputs video titles as clean, natural text under 100 characters without prefixes like `"The Topic:"` or markdown bold asterisks `**...**`.
2. **Mandatory CTA in Video Copy** — Ensure Studio AI always includes an engaging Call to Action (CTA) inviting viewers to like, subscribe to the channel, and comment when generating descriptions, metadata, or video copy.
3. **Topic & Category Context Awareness** — Pass the active video topic and selected category keys from the frontend to Studio AI, incorporating them into the Gemini prompt context so suggestions align with the user's active categories.
4. **Preserve Existing Capabilities** — Keep all existing YouTube metadata, hashtags, keywords, tags, category generation algorithms, Playlist Metadata, and UI layout completely intact.
5. **Preserve Gemini Integration** — Retain Google Gemini API (`gemini-3.8-flash`) via the OpenAI-compatible completions endpoint, rate limit handling, and API key resolution.
6. **Minimal Code Surface** — Accomplish all goals with minimal, targeted changes only to `ChatRequestDTO`, `ChatService`, `app.js`, and test coverage.

## Non-Goals (Out of Scope)

- Redesigning or changing the UI layout, colors, or CSS.
- Modifying the legacy root prototype `hashtag_generator.html`.
- Changing existing category or tag generation logic in `TagGeneratorService.java`.
- Refactoring unrelated controllers, services, or models.
- Adding database persistence for chat messages.

## Constraints

- Java 21, Spring Boot 3.3.5.
- Google Gemini API integration (`gemini-3.8-flash`).
- Backward-compatible JSON payloads for `/api/chat`.
- All tests must pass with zero failures.

## Success Criteria

- [x] `ChatRequestDTO` includes `selectedCategories` field with getter and setter.
- [x] `app.js` sends `selectedCategories` (from `activeCats`) and clean topic to `/api/chat`.
- [x] `ChatService` system prompt instructs Gemini to output clean titles (no "The Topic: **...**" or asterisks), mandatory CTAs in descriptions/metadata, and context from selected categories.
- [x] `ChatService` cleans any accidental "The Topic: **..." prefixes in generated responses.
- [x] Unit tests in `ChatServiceTest` verify clean topic extraction, prompt construction with category context & CTA, and title sanitization.
- [x] Existing metadata, tags, hashtags, and playlist generation remain completely unaltered and fully functional.

---

*Last updated: 2026-11-12*
