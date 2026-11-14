---
phase: 4
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
    - "Generated YouTube descriptions follow the complete 9-part ChatGPT structure (Hook, Topic Explanation, What You'll Learn, Key Takeaway, Target Audience, Engagement CTA, Like & Subscribe CTA, Brand Closing, and Hashtags)"
    - "Keyword stuffing, repetitive keyword prefixing (e.g. topic + basics, topic + tutorial), and comma-separated keyword dumping are eliminated"
    - "Unrelated category leakage (database design, clean code) and ungrounded claims (2026, full course free) are removed"
    - "Hashtags are concise, recognizable, and topic-specific (5-10 tags total)"
    - "Exact brand closing line 'TechNova IQ — Learn Smarter. Code Faster.' is preserved verbatim"
    - "Both engagement CTA and like & subscribe CTA are preserved"
    - "TagGeneratorService.java is strictly protected and remains 100% untouched"
    - "YouTube Shorts clipboard copy with zero-width space line preservation remains 100% functional"
---

# Plan 4.1: YouTube Description SEO Quality Enhancement

<objective>
Improve description readability, presentation, and natural SEO relevance in TechNova Tag Studio across video topics without rewriting the application, modifying TagGeneratorService.java, or changing UI styling.
</objective>

<context>
Baseline Review Test Case: "Azure DevOps Pipeline Optimization"
Protected files: TagGeneratorService.java, hashtag_generator.html, CSS files.
</context>

<tasks>
<task type="auto">
  <name>Implement 9-part ChatGPT structure and strict SEO quality rules in ChatService</name>
  <files>
    src/main/java/com/technova/tagstudio/service/ChatService.java
  </files>
  <action>
    1. Update ChatService.buildRequestBody system prompt to mandate the 9-part ChatGPT-style description structure and strict SEO quality & anti-stuffing rules.
    2. Implement optimizeDescription utility method to clean raw templates, strip repetitive prefixes, filter ungrounded claims, and curate hashtags.
  </action>
</task>

<task type="auto">
  <name>Integrate optimizeDescriptionTemplate in app.js</name>
  <files>
    src/main/resources/static/js/app.js
  </files>
  <action>
    1. Implement optimizeDescriptionTemplate in app.js matching the 9-part structure and anti-stuffing curation logic.
    2. Wire into buildMetadataTab and renderPromptContent so rendered descriptions are instantly clean and readable.
  </action>
</task>

<task type="auto">
  <name>Unit testing and empirical verification in ChatServiceTest</name>
  <files>
    src/test/java/com/technova/tagstudio/ChatServiceTest.java
  </files>
  <action>
    1. Add testStudioAiPromptEnforcesNinePartStructureAndAntiStuffing.
    2. Add testOptimizeDescriptionBaselineAzureDevopsPipelineOptimization using the baseline test case.
    3. Run full automated test suite to confirm zero regressions.
  </action>
</task>
</tasks>

