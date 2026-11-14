---
phase: 1
plan: 1
wave: 1
gap_closure: false
---

# Plan 1.1: Studio AI Clean Titles, CTA & Category Context Integration

## Objective
Fix and enhance Studio AI in TechNova Tag Studio with minimal, surgical changes:
1. Ensure Studio AI generates video titles where the topic appears as clean, natural text instead of `"The Topic: **...**"`.
2. Ensure Studio AI outputs include a compelling, dedicated Call to Action (CTA) when delivering descriptions or metadata suggestions.
3. Pass the active workspace topic and selected category context from the frontend into Studio AI's Gemini system prompt.
4. Strictly preserve all existing YouTube metadata, hashtags, keywords, category logic, Playlist Metadata, UI layout, and Gemini API integration.

## Context
Load these files for context:
- .gsd/SPEC.md
- src/main/java/com/technova/tagstudio/dto/ChatRequestDTO.java
- src/main/java/com/technova/tagstudio/service/ChatService.java
- src/main/resources/static/js/app.js
- src/test/java/com/technova/tagstudio/ChatServiceTest.java

## Tasks

<task type="auto">
  <name>Enhance ChatRequestDTO and ChatService with category context, clean title rules, and CTA instructions</name>
  <files>
    src/main/java/com/technova/tagstudio/dto/ChatRequestDTO.java
    src/main/java/com/technova/tagstudio/service/ChatService.java
  </files>
  <action>
    1. In `ChatRequestDTO.java`:
       - Add `private List<String> selectedCategories;` with standard getter `getSelectedCategories()` and setter `setSelectedCategories(List<String>)`.
    2. In `ChatService.java`:
       - In `processChat()`:
         - Sanitize `rawTopic` so markdown bolding (`**`, `*`, `__`, `_`) and `"The Topic:"` / `"Topic:"` prefixes are completely stripped, producing clean capitalized topic text.
         - Retrieve `selectedCategories` from `request.getSelectedCategories()`.
         - Pass `selectedCategories` to `buildRequestBody()` and `callGeminiWithRetry()`.
       - In `buildRequestBody(messages, topic, channel, selectedCategories)`:
         - Update the system prompt to explicitly enforce:
           a) **Clean Title Formatting**: "When generating YouTube titles, format the topic as clean natural text (e.g., 'SQL Server Performance Tuning') under 100 characters. Never output labels like 'The Topic:', 'Topic:', or markdown bold asterisks (**) around the topic or title."
           b) **Mandatory CTA**: "When generating YouTube descriptions, video copy, or metadata recommendations, always include a dedicated Call to Action (CTA) section encouraging viewers to like the video, subscribe to the channel" + (!channel.isEmpty() ? " (" + channel + ")" : "") + ", and drop comments."
           c) **Category & Topic Context**: If `selectedCategories` is provided and non-empty, append: "Active categories selected in the workspace: " + String.join(", ", selectedCategories) + ". Contextually incorporate these category angles and keywords into recommendations."
       - In `processChat()` reply handling:
         - Add a post-processing cleanup on `reply` to remove any accidental `"The Topic: **"` or `"The Topic:"` prefixes from line headings if Gemini inadvertently mirrors them.
    
    AVOID: Modifying unrelated services (`TagGeneratorService`), controllers, or UI layout.
    AVOID: Changing the Gemini API endpoint or model configurations.
    USE: Minimal, surgical additions to `ChatRequestDTO` and `ChatService`.
  </action>
  <verify>
    Verify compilation and that `ChatRequestDTO` and `ChatService` compile without errors.
  </verify>
  <done>
    `ChatRequestDTO` accepts `selectedCategories`; `ChatService` system prompt incorporates category context, enforces clean titles, mandates CTAs, and sanitizes output.
  </done>
</task>

<task type="auto">
  <name>Pass selected categories from app.js and add automated unit tests</name>
  <files>
    src/main/resources/static/js/app.js
    src/test/java/com/technova/tagstudio/ChatServiceTest.java
  </files>
  <action>
    1. In `src/main/resources/static/js/app.js`:
       - In `sendStudioAiMessage()`:
         - Sanitize `topic` before sending using `cleanTopicString(currentTopic())`.
         - Extract active categories: `const selectedCategories = Array.from(activeCats);`.
         - Include `selectedCategories` in the JSON body sent to `/api/chat`:
           `body: JSON.stringify({ channel, topic, selectedCategories, messages: chatHistory })`
       - Ensure zero modifications to the chat UI layout, CSS, bubble trigger, or window behavior.
    2. In `src/test/java/com/technova/tagstudio/ChatServiceTest.java`:
       - Add a test `testStudioAiRequestWithCategoriesAndCleanTopic()`:
         - Verify that `ChatRequestDTO` properly holds `selectedCategories`.
         - Verify that passing topics with `"The Topic: **Azure DevOps**"` to `ChatService` does not throw, sanitizes the topic cleanly, and incorporates active categories.
       - Add a test `testCleanTitleAndCtaPromptInstructions()`:
         - Verify the system prompt construction logic includes clean title instructions, mandatory CTA requirements, channel reference, and selected categories context.
       - Verify all existing tests in `ChatServiceTest` continue to pass.
    
    AVOID: Changing HTML or CSS files.
    AVOID: Modifying `hashtag_generator.html`.
    USE: Existing `cleanTopicString` and `activeCats` helpers in `app.js`.
  </action>
  <verify>
    Run `mvnw test -Dtest=ChatServiceTest` or verify that all test assertions in `ChatServiceTest` pass.
  </verify>
  <done>
    Frontend sends active category and clean topic context to `/api/chat`, and backend tests verify clean title formatting, category inclusion, and CTA prompt requirements.
  </done>
</task>

## Must-Haves
After all tasks complete, verify:
- [ ] Studio AI titles present topic as clean text without `"The Topic: **..."`
- [ ] Studio AI system prompt mandates inclusion of Call to Action (CTA) in descriptions/metadata
- [ ] Studio AI prompt includes active topic and selected category keys
- [ ] Existing YouTube metadata, tags, hashtags, and playlist generation remain unchanged
- [ ] Google Gemini integration remains unchanged
- [ ] Code modifications are strictly confined to `ChatRequestDTO.java`, `ChatService.java`, `app.js`, and `ChatServiceTest.java`

## Success Criteria
- [ ] Zero UI redesign or CSS changes
- [ ] Zero changes to `hashtag_generator.html`
- [ ] All tests in `ChatServiceTest` pass
- [ ] Minimal code diff achieved
