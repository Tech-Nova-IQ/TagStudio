---
phase: 3
plan: 1
wave: 1
depends_on: []
files_modified:
  - src/main/java/com/technova/tagstudio/service/ChatService.java
  - src/main/resources/static/js/app.js
  - src/test/java/com/technova/tagstudio/ChatServiceTest.java
autonomous: true
user_setup: []

must_haves:
  truths:
    - "YouTube description copy preserves all paragraph breaks and blank lines when pasted into YouTube Shorts description box"
    - "Zero-width spaces (\\u200B) on blank lines prevent YouTube Shorts description box from collapsing empty lines"
    - "Emojis and Unicode characters (🚀, 🔹, 💡, 🎯, 📌, 👍, 🔔, em dashes, etc.) remain 100% intact"
    - "Bullet points (e.g. * Pipeline Artifacts = ...) remain intact on separate lines"
    - "Markdown asterisks for bolding (**...**) are cleaned to plain text, while intentional bullet asterisks remain"
    - "No unwanted HTML tags (<br>, <strong>, etc.) or literal \\n escape sequences appear in copied text"
    - "Studio AI chat responses provide a copy button that outputs clean, publish-ready text"
    - "All other copy buttons (title, tags, hashtags, keywords, playlist, prompt) continue to work normally without regression"
    - "Existing UI styling and CSS are completely unchanged"
  artifacts:
    - "src/main/resources/static/js/app.js contains formatYouTubeDescription and updated copy handlers"
    - "src/main/java/com/technova/tagstudio/service/ChatService.java contains formatYouTubeDescription utility method"
    - "src/test/java/com/technova/tagstudio/ChatServiceTest.java contains comprehensive unit tests verifying the formatting rules against the reference description"
---

# Plan 3.1: YouTube Description Copy Formatting & Shorts Compatibility Fix

<objective>
Fix the YouTube description copy functionality in TechNova Tag Studio so that when a user clicks Copy and pastes the description into the YouTube Shorts description textbox, the text retains the exact formatting (all paragraph breaks, blank lines between sections, emojis, bullet points, hashtags, CTAs, and brand closing line) without unwanted HTML tags, literal escape sequences, collapsed whitespace, or raw markdown bold asterisks.
</objective>

<context>
Load for context:
- .gsd/SPEC.md
- .gsd/ROADMAP.md
- src/main/resources/static/js/app.js
- src/main/java/com/technova/tagstudio/service/ChatService.java
- src/test/java/com/technova/tagstudio/ChatServiceTest.java
</context>

<tasks>

<task type="auto">
  <name>Implement formatYouTubeDescription algorithm and JUnit 5 unit tests in ChatServiceTest</name>
  <files>
    src/main/java/com/technova/tagstudio/service/ChatService.java
    src/test/java/com/technova/tagstudio/ChatServiceTest.java
  </files>
  <action>
    1. In `ChatService.java`, implement `public String formatYouTubeDescription(String rawText)`:
       - Handle null / empty string gracefully (return empty string).
       - Convert literal `\n` (`\\n`) and `\r` (`\\r`) escape sequences into real newlines.
       - Normalize line breaks (`\r\n` and `\r` to `\n`).
       - Convert HTML break tags (`<br>`, `<br/>`, `<br />`) to `\n` and strip HTML tags (e.g. `<strong>`, `<p>`, `<div>`).
       - Strip any "Studio AI" label prefix if present at the start.
       - Strip markdown bolding `**text**` -> `text` using regex, while preserving single asterisk list bullets (`* `) and unicode bullets.
       - Strip markdown headers (`# `, `## `, `### `).
       - Prevent YouTube Shorts blank-line collapse: split text by `\n` and for any empty or whitespace-only line, assign an invisible Unicode zero-width space (`\u200B`). When joined with `\n`, YouTube's textbox sees character content on the line and will not collapse empty lines.
       - Ensure all emojis, Unicode characters (🚀, 🔹, 💡, 🎯, 📌, 👍, 🔔, —, etc.), hashtags, CTAs, bullets, and punctuation remain strictly intact.
    2. In `ChatServiceTest.java`:
       - Add unit test `testFormatYouTubeDescriptionWithReferenceShort()` using the exact Reference Description from the specification (Pipeline Artifacts vs Azure Artifacts Feeds).
       - Assert all 11 sections and blank lines are preserved with zero-width spaces.
       - Assert emojis and Unicode characters are intact.
       - Assert bullet points remain on separate lines with `* `.
       - Assert markdown asterisks `**...**` are cleaned.
       - Assert hashtags `#AzureDevOps #PipelineArtifacts ...` remain at the bottom.
       - Assert brand closing line `Learn Smarter. Code Faster.` is preserved.
       - Assert no HTML tags or literal `\n` appear.
       - Add edge case tests for literal `\n`, HTML tags, mixed CRLF/LF, and multiple blank lines.
    
    AVOID: Modifying TagGeneratorService.java or altering Gemini model configuration.
    AVOID: Collapsing multiple blank lines into a single newline.
  </action>
  <verify>
    Run `.\mvnw test -Dtest=ChatServiceTest` and confirm all tests pass.
  </verify>
  <done>
    `formatYouTubeDescription` algorithm correctly formats descriptions and all unit tests in `ChatServiceTest` pass with 100% success rate.
  </done>
</task>

<task type="auto">
  <name>Integrate formatYouTubeDescription in app.js and wire copy buttons</name>
  <files>
    src/main/resources/static/js/app.js
  </files>
  <action>
    1. In `app.js`, define `formatYouTubeDescription(rawText)` matching the exact formatting logic:
       - Handle null/undefined/empty string.
       - Replace literal `\\n` with `\n`.
       - Replace `<br\s*/?>` with `\n` and strip HTML tags (`/<[^>]+>/g`).
       - Remove leading "Studio AI" / "Studio AI:" header if present.
       - Clean markdown bolding `/\*\*(.*?)\*\*/g` -> `$1` without touching bullet asterisks `* `.
       - Strip markdown header hashes `^#{1,6}\s+`.
       - Normalize line breaks (`\r\n` and `\r` to `\n`).
       - Split by `\n` and replace empty lines with `\u200B` to prevent YouTube whitespace collapse.
    2. Update `buildMetadataTab()`:
       - On `#copyDesc` button click, pass `formatYouTubeDescription(document.getElementById('descArea').value)` to `safeCopy()`.
    3. Update `renderPromptContent()`:
       - On `#${entryId}_copyPromptDesc` button click, pass `formatYouTubeDescription(document.getElementById(descAreaId).value)` to `safeCopy()`.
    4. Update `sendStudioAiMessage()`:
       - In `botMsgDiv`, add a neat `copy` button (styled with the existing `.copybtn` class) in the message header row next to "Studio AI".
       - On clicking the bot message copy button, call `safeCopy(formatYouTubeDescription(data.reply), 'Description copied')`.
    5. Maintain strict non-regression:
       - All other copy buttons (titles, CTA, category tags, hashtags, keywords, playlist tab, prompt) continue to call `safeCopy` directly without alteration.
    
    AVOID: Modifying style.css or altering existing UI layout or CSS.
    AVOID: Modifying legacy hashtag_generator.html.
  </action>
  <verify>
    Inspect `app.js` to ensure no syntax errors and verify that other copy buttons (title, CTA, tags, hashtags, keywords) remain untouched.
  </verify>
  <done>
    Description copy buttons in Shorts Metadata, Prompt results, and Studio AI chat messages all invoke `formatYouTubeDescription` before copying to clipboard.
  </done>
</task>

<task type="auto">
  <name>Comprehensive test suite execution and empirical verification</name>
  <files>
    src/test/java/com/technova/tagstudio/ChatServiceTest.java
    src/main/resources/static/js/app.js
  </files>
  <action>
    1. Run `.\mvnw test` to execute all tests across `ChatServiceTest`.
    2. Confirm that all 10 existing tests + new description formatting tests pass with zero failures.
    3. Empirically verify that the output of `formatYouTubeDescription` matches the required structure:
       - Opening paragraph
       - Blank line (\u200B)
       - First info section
       - Blank line (\u200B)
       - Second info section
       - Blank line (\u200B)
       - Remember section with separate bullet lines
       - Blank line (\u200B)
       - Explanatory paragraph
       - Blank line (\u200B)
       - Audience/context paragraph
       - Blank line (\u200B)
       - Engagement CTA
       - Blank line (\u200B)
       - Like CTA
       - Blank line (\u200B)
       - Subscribe CTA
       - Blank line (\u200B)
       - Brand closing line
       - Blank line (\u200B)
       - Hashtags
  </action>
  <verify>
    `.\mvnw test` finishes with `BUILD SUCCESS` and 0 failures.
  </verify>
  <done>
    Entire test suite passes cleanly, regression tests pass, and YouTube Shorts formatting contract is verified.
  </done>
</task>

</tasks>

<verification>
After all tasks, verify:
- [ ] Copied description preserves paragraph breaks and blank lines when pasted into YouTube Shorts description box
- [ ] Emojis and Unicode characters remain intact
- [ ] Bullet points remain on separate lines
- [ ] Hashtags remain intact and correctly positioned
- [ ] Pasted content contains no unwanted HTML tags
- [ ] No literal `\n` escape sequences appear in the pasted description
- [ ] No Markdown asterisks appear unless intentionally part of bullet points
- [ ] Copied description matches the 12-section intended text structure
- [ ] Other copy buttons continue to work as before
- [ ] Existing UI styling and responsive behavior remain unchanged
- [ ] All unit tests pass with zero failures
</verification>

<success_criteria>
- [ ] All 3 tasks verified
- [ ] Must-haves confirmed
- [ ] Zero regressions in existing features
</success_criteria>
