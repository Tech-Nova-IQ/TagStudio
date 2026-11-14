package com.technova.tagstudio;

import com.technova.tagstudio.dto.ChatMessageDTO;
import com.technova.tagstudio.dto.ChatRequestDTO;
import com.technova.tagstudio.dto.ChatResponseDTO;
import com.technova.tagstudio.dto.TagStudioRequestDTO;
import com.technova.tagstudio.dto.TagStudioResponseDTO;
import com.technova.tagstudio.service.ChatService;
import com.technova.tagstudio.service.TagGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:chattestdb;DB_CLOSE_DELAY=-1"
})
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private TagGeneratorService tagGeneratorService;

    @Test
    public void testEmptyMessagesValidation() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setMessages(new ArrayList<>());
        ChatResponseDTO resp = chatService.processChat(req);
        assertNotNull(resp.getError());
    }

    @Test
    public void testConversationalQuestionHandling() {
        ChatRequestDTO req = new ChatRequestDTO();
        List<ChatMessageDTO> list = new ArrayList<>();
        list.add(new ChatMessageDTO("user", "How are you?"));
        req.setMessages(list);
        req.setTopic("Azure DevOps");

        ChatResponseDTO resp = chatService.processChat(req);
        assertNotNull(resp);
        // If API key is not configured, it must return a clear configuration error,
        // never hardcoded response
        // If API key is configured, it must return an answer or an explicit API error
        // (never hardcoded SEO)
        if (resp.getError() != null) {
            assertFalse(resp.getError().contains("Here are some recommendations"),
                    "Must never return hardcoded SEO fallback");
            assertTrue(
                    resp.getError().contains("GEMINI_API_KEY") || resp.getError().contains("Gemini")
                            || resp.getError().contains("Rate Limit") || resp.getError().contains("Quota")
                            || resp.getError().contains("Studio AI") || resp.getError().contains("timed out")
                            || resp.getError().contains("connect"),
                    "Error must be informative: " + resp.getError());
        } else {
            assertNotNull(resp.getReply());
            assertFalse(resp.getReply().contains("Here are high-performing hashtag ideas for"),
                    "Must not return canned SEO tags");
        }
    }

    @Test
    public void testUnrelatedQuestionHandling() {
        ChatRequestDTO req = new ChatRequestDTO();
        List<ChatMessageDTO> list = new ArrayList<>();
        list.add(new ChatMessageDTO("user", "What is SQL Server?"));
        req.setMessages(list);
        req.setTopic("Azure DevOps");

        ChatResponseDTO resp = chatService.processChat(req);
        assertNotNull(resp);
        if (resp.getError() != null) {
            assertFalse(resp.getError().contains("Here are some recommendations"));
        } else {
            assertNotNull(resp.getReply());
            assertFalse(resp.getReply().contains("Here are high-performing hashtag ideas for"));
        }
    }

    @Test
    public void testDuplicateRequestDeduplication() {
        ChatRequestDTO req = new ChatRequestDTO();
        List<ChatMessageDTO> list = new ArrayList<>();
        list.add(new ChatMessageDTO("user", "Duplicate test question"));
        req.setMessages(list);

        chatService.processChat(req);
        // Immediate repeat must be detected as duplicate
        ChatResponseDTO resp2 = chatService.processChat(req);
        assertNotNull(resp2);
        assertTrue(resp2.getError() != null && resp2.getError().contains("Duplicate request detected"));
    }

    @Test
    public void testPromptTabCleanTopicExtraction() {
        TagStudioRequestDTO req1 = new TagStudioRequestDTO();
        req1.setPrompt("The Topic: Azure DevOps Interview Questions");
        TagStudioResponseDTO resp1 = tagGeneratorService.generateStudioData(req1);
        assertEquals("Azure DevOps Interview Questions", resp1.getVideoTopic());
        assertFalse(resp1.getVideoTopic().contains("The Topic"));
        assertFalse(resp1.getMetadata().getTitleOptions().get(0).contains("The Topic:"));
        assertFalse(resp1.getMetadata().getDescriptionTemplate().contains("the topic azure devops"));
        assertFalse(resp1.getCategories().get(0).getHashtags().contains("#TheTopicAzureDevopsTutorial"));

        TagStudioRequestDTO req2 = new TagStudioRequestDTO();
        req2.setPrompt("The Topic: **Azure DevOps Interview Questions**");
        TagStudioResponseDTO resp2 = tagGeneratorService.generateStudioData(req2);
        assertEquals("Azure DevOps Interview Questions", resp2.getVideoTopic());

        TagStudioRequestDTO req3 = new TagStudioRequestDTO();
        req3.setPrompt("The topic azure devops interview questions");
        TagStudioResponseDTO resp3 = tagGeneratorService.generateStudioData(req3);
        assertEquals("Azure DevOps Interview Questions", resp3.getVideoTopic());

        TagStudioRequestDTO req4 = new TagStudioRequestDTO();
        req4.setPrompt("Azure DevOps Interview Questions");
        TagStudioResponseDTO resp4 = tagGeneratorService.generateStudioData(req4);
        assertEquals("Azure DevOps Interview Questions", resp4.getVideoTopic());
    }

    @Test
    public void testPromptAndMetadataCtaGeneration() {
        TagStudioRequestDTO req = new TagStudioRequestDTO();
        req.setChannelName("TechNova IQ");
        req.setPrompt("The Topic: Docker Containerization Best Practices");
        TagStudioResponseDTO resp = tagGeneratorService.generateStudioData(req);

        assertNotNull(resp);
        assertNotNull(resp.getMetadata());
        assertNotNull(resp.getMetadata().getCtaText(), "MetadataDTO must contain ctaText");
        assertTrue(resp.getMetadata().getCtaText().contains("TechNova IQ"));
        assertTrue(resp.getMetadata().getCtaText().contains("Like the video"));
        assertTrue(resp.getMetadata().getCtaText().contains("Subscribe"));

        assertNotNull(resp.getPlaylistMetadata());
        assertNotNull(resp.getPlaylistMetadata().getCtaText(), "PlaylistMetadataDTO must contain ctaText");
        assertTrue(resp.getPlaylistMetadata().getCtaText().contains("TechNova IQ"));
    }

    @Test
    public void testStudioAiCleanTopicAndSanitizeReply() {
        String clean1 = chatService.cleanTopicString("The Topic: **Azure DevOps Interview Questions**");
        assertEquals("Azure DevOps Interview Questions", clean1);

        String clean2 = chatService.cleanTopicString("the topic is: sql server performance tuning");
        assertEquals("SQL Server Performance Tuning", clean2);

        String clean3 = chatService.cleanTopicString("My Topic: AWS Cloud Architecture");
        assertEquals("AWS Cloud Architecture", clean3);

        // Verify sanitizeReply strips unwanted "The Topic: **...**" and "The Topic: ..."
        String sampleReply = "Here are your titles:\n" +
                "1. The Topic: **SQL Server Performance Tuning** Explained #Shorts\n" +
                "2. The Topic: SQL Server Tuning for Beginners\n" +
                "3. Master SQL Server Indexing in 60 Seconds";
        String sanitized = chatService.sanitizeReply(sampleReply);
        assertFalse(sanitized.contains("The Topic: **"));
        assertFalse(sanitized.contains("The Topic:"));
        assertTrue(sanitized.contains("SQL Server Performance Tuning Explained #Shorts"));
        assertTrue(sanitized.contains("SQL Server Tuning for Beginners"));
    }

    @Test
    public void testStudioAiRequestBodyWithCategoriesCleanTitleAndCta() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setChannel("TechNova IQ");
        req.setTopic("The Topic: **Azure DevOps CI/CD**");
        req.setSelectedCategories(List.of("DevOps", "Monitoring", "SEO"));

        List<ChatMessageDTO> msgs = List.of(new ChatMessageDTO("user", "Suggest video titles and a description."));
        req.setMessages(msgs);

        String cleanTopic = chatService.cleanTopicString(req.getTopic());
        assertEquals("Azure DevOps CI/CD", cleanTopic);

        String body = chatService.buildRequestBody(msgs, cleanTopic, req.getChannel(), req.getSelectedCategories());
        assertNotNull(body);
        assertTrue(body.contains("Azure DevOps CI/CD"));
        assertTrue(body.contains("TechNova IQ"));
        assertTrue(body.contains("Active categories selected in the workspace: DevOps, Monitoring, SEO"));
        assertTrue(body.contains("When generating YouTube titles, format the topic as clean natural text"));
        assertTrue(body.contains("Never output labels like 'The Topic:', 'Topic:', or markdown bold asterisks (**)"));
        assertTrue(body.contains("always include a dedicated Call to Action (CTA)"));
    }

    @Test
    public void testStudioAiErrorCategorization() {
        // 1. 404 Model or endpoint not found
        ChatService.ErrorInfo err404 = new ChatService.ErrorInfo("models/gemini-3.8-flash not found", "NOT_FOUND", "models/gemini-3.8-flash not found");
        ChatResponseDTO resp404 = chatService.formatErrorResponse(404, err404);
        assertNotNull(resp404.getError());
        assertTrue(resp404.getError().contains("Model or endpoint not found"));

        // 2. 401 / 403 Authentication failure
        ChatService.ErrorInfo err401 = new ChatService.ErrorInfo("API_KEY_INVALID", "UNAUTHENTICATED", "API key not valid.");
        ChatResponseDTO resp401 = chatService.formatErrorResponse(401, err401);
        assertNotNull(resp401.getError());
        assertTrue(resp401.getError().contains("Authentication failed"));

        // 3. 400 Bad request
        ChatService.ErrorInfo err400 = new ChatService.ErrorInfo("INVALID_ARGUMENT", "BAD_REQUEST", "Field content cannot be empty");
        ChatResponseDTO resp400 = chatService.formatErrorResponse(400, err400);
        assertNotNull(resp400.getError());
        assertTrue(resp400.getError().contains("Invalid request parameters"));
        assertTrue(resp400.getError().contains("Field content cannot be empty"));

        // 4. 429 Quota exhaustion
        ChatService.ErrorInfo errQuota = new ChatService.ErrorInfo("RESOURCE_EXHAUSTED", "insufficient_quota", "Quota exceeded for quota metric");
        ChatResponseDTO respQuota = chatService.formatErrorResponse(429, errQuota);
        assertNotNull(respQuota.getError());
        assertTrue(respQuota.getError().contains("Quota Exceeded"));

        // 5. 429 Rate limit (transient)
        ChatService.ErrorInfo errRate = new ChatService.ErrorInfo("RATE_LIMIT_EXCEEDED", "RESOURCE_EXHAUSTED", "Rate limit reached for requests per minute");
        ChatResponseDTO respRate = chatService.formatErrorResponse(429, errRate);
        assertNotNull(respRate.getError());
        assertTrue(respRate.getError().contains("Rate Limit"));

        // 6. 503 Service unavailable / high demand
        ChatService.ErrorInfo err503 = new ChatService.ErrorInfo("503", "UNAVAILABLE", "This model is currently experiencing high demand.");
        ChatResponseDTO resp503 = chatService.formatErrorResponse(503, err503);
        assertNotNull(resp503.getError());
        assertTrue(resp503.getError().contains("temporarily experiencing high demand"));
        assertTrue(resp503.getError().contains("503"));
    }

    @Test
    public void testParseErrorJsonRobustness() {
        String json = "{\"error\":{\"code\":404,\"message\":\"Model not found\",\"status\":\"NOT_FOUND\"}}";
        ChatService.ErrorInfo info = chatService.parseError(json);
        assertNotNull(info);
        assertEquals("404", info.code);
        assertEquals("NOT_FOUND", info.type);
        assertEquals("Model not found", info.message);
    }

    @Test
    public void testFormatYouTubeDescriptionWithReferenceShort() {
        String referenceDescription =
            "# Title\n\n" +
            "How to Speed Up Slow Builds in Azure DevOps #Shorts\n\n" +
            "# Description\n\n" +
            "Is your Azure DevOps pipeline taking too long to finish? 🚀 Stop repeating work and start optimizing your build pipeline!\n\n" +
            "In this Short, learn practical ways to speed up slow Azure DevOps builds by identifying bottlenecks, caching dependencies, and running independent jobs in parallel.\n\n" +
            "🔹 **What you'll learn:**\n" +
            "- Identify slow pipeline stages such as checkout, restore, build, and test.\n" +
            "- Use `Cache@2` to reuse cached dependency data.\n" +
            "- Understand why `npm ci` can still run when a cache is available.\n" +
            "- Parallelize independent jobs when sufficient agent capacity is available.\n" +
            "- Measure build performance before making optimization changes.\n\n" +
            "**Key takeaway:** Measure first, cache repeated downloads, and parallelize where it makes sense. Results depend on your pipeline configuration, dependencies, and available resources.\n\n" +
            "💬 Which stage takes the longest in your Azure DevOps pipeline: Restore, Build, or Test? Share your experience in the comments!\n\n" +
            "👍 Like this video and subscribe to **TechNova IQ** for practical Azure DevOps tutorials, CI/CD tips, cloud engineering, and technical interview preparation.\n\n" +
            "TechNova IQ — Learn Smarter. Code Faster.\n\n" +
            "#AzureDevOps #CICD #PipelineOptimization #BuildPerformance #Shorts";

        String formatted = chatService.formatYouTubeDescription(referenceDescription);

        assertNotNull(formatted);
        // 1. Markdown bold asterisks must be cleaned
        assertFalse(formatted.contains("**What you'll learn:**"), "Markdown bolding must be stripped");
        assertTrue(formatted.contains("What you'll learn:"), "Cleaned header text must remain");
        assertFalse(formatted.contains("**Key takeaway:**"));
        assertTrue(formatted.contains("Key takeaway:"));
        assertFalse(formatted.contains("**TechNova IQ**"));
        assertTrue(formatted.contains("TechNova IQ"));

        // 2. Invisible zero-width spaces (\u200B) must be inserted on blank lines to prevent YouTube Shorts collapse
        assertTrue(formatted.contains("\u200B"), "Must contain \\u200B on blank lines for YouTube Shorts compatibility");

        // 3. Emojis and Unicode characters must remain 100% intact
        assertTrue(formatted.contains("🚀"));
        assertTrue(formatted.contains("🔹"));
        assertTrue(formatted.contains("💬"));
        assertTrue(formatted.contains("👍"));
        assertTrue(formatted.contains("—"));

        // 4. Bullet points must be on separate lines
        assertTrue(formatted.contains("- Identify slow pipeline stages"));
        assertTrue(formatted.contains("- Use `Cache@2`"));
        assertTrue(formatted.contains("- Measure build performance"));

        // 5. Hashtags must remain at the bottom intact
        assertTrue(formatted.contains("#AzureDevOps #CICD #PipelineOptimization #BuildPerformance #Shorts"));

        // 6. Brand closing line must remain intact
        assertTrue(formatted.contains("TechNova IQ — Learn Smarter. Code Faster."));

        // 7. No HTML tags or markdown header hashes at start
        assertFalse(formatted.contains("<"));
        assertFalse(formatted.contains(">"));
    }

    @Test
    public void testFormatYouTubeDescriptionHtmlEntitiesAndTags() {
        String raw = "<strong>Studio AI:</strong> Check this &amp; that &nbsp;&#x20;for details.<br><br>Second paragraph &quot;quoted&#39; text.";
        String formatted = chatService.formatYouTubeDescription(raw);

        assertNotNull(formatted);
        assertFalse(formatted.contains("Studio AI:"), "Prefix must be stripped");
        assertFalse(formatted.contains("<strong>"), "HTML tags must be stripped");
        assertFalse(formatted.contains("<br>"), "Break tags must be converted");
        assertTrue(formatted.contains("&"), "Entities like &amp; must decode to &");
        assertTrue(formatted.contains("\"quoted' text"), "Quotes must decode properly");
        assertTrue(formatted.contains("\u200B"), "Blank lines between paragraphs must contain \\u200B");
    }

    @Test
    public void testFormatYouTubeDescriptionLiteralEscapesAndEdgeCases() {
        assertEquals("", chatService.formatYouTubeDescription(null));
        assertEquals("", chatService.formatYouTubeDescription("   "));

        String rawEscaped = "First paragraph\\n\\nSecond paragraph with literal escape.";
        String formatted = chatService.formatYouTubeDescription(rawEscaped);
        assertTrue(formatted.contains("First paragraph\n\u200B\nSecond paragraph"));
    }

    @Test
    public void testStudioAiPromptEnforcesChatGPTStructureAndNoInternalNotes() {
        List<ChatMessageDTO> msgs = List.of(new ChatMessageDTO("user", "Write a Shorts description"));
        String body = chatService.buildRequestBody(msgs, "Azure DevOps", "TechNova IQ", List.of("DevOps"));

        assertNotNull(body);
        assertTrue(body.contains("ChatGPT-style"), "Prompt must instruct ChatGPT-style structure");
        assertTrue(body.contains("What You'll Learn"), "Prompt must include What You'll Learn");
        assertTrue(body.contains("Key Takeaway"), "Prompt must include Key Takeaway");
        assertTrue(body.contains("STRICT OUTPUT CLEANLINESS"), "Prompt must enforce strict output cleanliness");
        assertTrue(body.contains("Never include internal notes"), "Prompt must forbid internal notes");
    }

    @Test
    public void testStudioAiPromptEnforcesNinePartStructureAndAntiStuffing() {
        List<ChatMessageDTO> msgs = List.of(new ChatMessageDTO("user", "Generate an SEO-optimized video description"));
        String body = chatService.buildRequestBody(msgs, "Azure DevOps Pipeline Optimization", "TechNova IQ", List.of("DevOps"));

        assertNotNull(body);
        assertTrue(body.contains("9-part structure"), "Prompt must mandate 9-part structure");
        assertTrue(body.contains("Target Audience"), "Prompt must include Target Audience");
        assertTrue(body.contains("STRICT SEO QUALITY & ANTI-STUFFING"), "Prompt must mandate SEO quality & anti-stuffing");
        assertTrue(body.contains("Identify ONE primary keyword"), "Prompt must instruct primary keyword identification");
        assertTrue(body.contains("Strictly eliminate keyword stuffing"), "Prompt must forbid keyword stuffing");
        assertTrue(body.contains("Never invent course, free-content, year, or beginner claims"), "Prompt must forbid ungrounded claims");
        assertTrue(body.contains("concise, recognizable, topic-specific tags"), "Prompt must enforce concise hashtags");
    }

    @Test
    public void testOptimizeDescriptionBaselineAzureDevopsPipelineOptimization() {
        String baselineRawDescription =
            "Learn how Azure DevOps Pipeline Optimization works with practical, real-world walkthroughs from TechNova IQ to improve your development workflow and technical skills.\n\n" +
            "In this walkthrough, you'll explore azure devops pipeline optimization configuration, azure devops pipeline optimization architecture deep dive, azure devops pipeline optimization performance tuning, azure devops pipeline optimization tutorial, azure devops pipeline optimization basics, azure devops pipeline optimization explained, azure devops pipeline optimization 2026, azure devops pipeline optimization full course free, azure devops pipeline optimization for absolute beginners, database design, database normalization, clean code, coding best practices, YouTube Shorts, Shorts tutorial, helping you master essential techniques and practical concepts.\n\n" +
            "Perfect for Azure DevOps Pipeline Optimization beginners, developers, DevOps engineers, DBAs, QA engineers, cloud professionals, and interview preparation.\n\n" +
            "TechNova IQ — Learn Smarter. Code Faster.\n\n" +
            "#AzureDevopsPipelineOptimizationTutorial #AzureDevopsPipelineOptimizationBasics #AzureDevopsPipelineOptimizationExplained\n\n" +
            "What Azure DevOps Pipeline Optimization strategy or technique do you use most — or what's your biggest challenge with it? Share below! 👇\n\n" +
            "If this helped you understand Azure DevOps Pipeline Optimization, like 👍 and subscribe to TechNova IQ for more practical tutorials and technical concepts.";

        String optimized = chatService.optimizeDescription(baselineRawDescription, "Azure DevOps Pipeline Optimization", "TechNova IQ");

        assertNotNull(optimized);

        // 1. All 9 sections must be present in the structured description
        assertTrue(optimized.contains("Learn how Azure DevOps Pipeline Optimization works"), "1. Opening Hook must be present");
        assertTrue(optimized.contains("In this video, learn practical approaches"), "2. Topic Explanation must be present");
        assertTrue(optimized.contains("🔹 **What you'll learn:**"), "3. What You'll Learn header must be present");
        assertTrue(optimized.contains("**Key takeaway:**"), "4. Key Takeaway must be present");
        assertTrue(optimized.contains("**Target audience:**"), "5. Target Audience must be present");
        assertTrue(optimized.contains("💬"), "6. Engagement CTA question must be present");
        assertTrue(optimized.contains("👍"), "7. Like & Subscribe CTA must be present");
        assertTrue(optimized.contains("TechNova IQ — Learn Smarter. Code Faster."), "8. Brand Closing line must be exact");
        assertTrue(optimized.contains("#AzureDevOpsPipelineOptimization"), "9. Hashtags section must be present");

        // 2. Repetitive keyword variations and ungrounded claims must be eliminated
        assertFalse(optimized.contains("azure devops pipeline optimization basics"), "Repetitive basics keyword must be removed");
        assertFalse(optimized.contains("azure devops pipeline optimization 2026"), "Ungrounded year 2026 claim must be removed");
        assertFalse(optimized.contains("azure devops pipeline optimization full course free"), "Ungrounded course claim must be removed");
        assertFalse(optimized.contains("azure devops pipeline optimization for absolute beginners"), "Ungrounded absolute beginners claim must be removed");

        // 3. Unrelated category leakage must be eliminated
        assertFalse(optimized.contains("database design"), "Unrelated database design category must be removed");
        assertFalse(optimized.contains("database normalization"), "Unrelated database normalization category must be removed");
        assertFalse(optimized.contains("clean code"), "Unrelated clean code category must be removed");

        // 4. Hashtags must be concise and recognizable (5-10 tags total)
        assertFalse(optimized.contains("#AzureDevopsPipelineOptimizationTutorial"), "Bloated compound hashtags must be removed");
        assertFalse(optimized.contains("#AzureDevopsPipelineOptimizationBasics"), "Bloated compound hashtags must be removed");
        assertTrue(optimized.contains("#CICD"));
        assertTrue(optimized.contains("#DevOps"));
        assertTrue(optimized.contains("#PipelineOptimization"));
        assertTrue(optimized.contains("#TechNovaIQ"));

        // 5. When passed through formatYouTubeDescription for YouTube Shorts copy, zero-width spaces are retained
        String formattedForShorts = chatService.formatYouTubeDescription(optimized);
        assertTrue(formattedForShorts.contains("\u200B"), "Must contain \\u200B on blank lines for YouTube Shorts compatibility");
        assertFalse(formattedForShorts.contains("**What you'll learn:**"), "Markdown bolding must be stripped in clipboard output");
        assertTrue(formattedForShorts.contains("What you'll learn:"));
    }

    @Test
    public void testOptimizeDescriptionBranchPoliciesAndQuestionTopics() {
        String rawBranchPolicyDescription =
            "Learn how What Branch Policies Enforces works with practical, real-world walkthroughs from TechNova IQ to improve your development workflow and technical skills.\n\n" +
            "In this walkthrough, you'll explore what branch policies enforces configuration, what branch policies enforces architecture deep dive, what branch policies enforces performance tuning, what branch policies enforces for absolute beginners, what branch policies enforces step by step guide, helping you master essential techniques and practical concepts.\n\n" +
            "Perfect for What Branch Policies Enforces beginners, developers, DevOps engineers, DBAs, QA engineers, cloud professionals, and interview preparation.\n\n" +
            "TechNova IQ — Learn Smarter. Code Faster.\n\n" +
            "#AzureDevOps #AzureReposBranchPolicies #BuildValidationPolicy #MinimumReviewersGit\n\n" +
            "What what branch policies enforces strategy or technique do you use most — or what's your biggest challenge with it? Share below! 👇\n\n" +
            "If this helped you understand What Branch Policies Enforces, like 👍 and subscribe to TechNova IQ for more practical tutorials and technical concepts.";

        String optimized = chatService.optimizeDescription(rawBranchPolicyDescription, "What Branch Policies Enforces", "TechNova IQ");

        assertNotNull(optimized);

        // 1. Clean core subject ("Branch Policies") extracted without awkward "What Branch Policies Enforces" or "What what"
        assertTrue(optimized.contains("Learn how Branch Policies work"), "Hook must use clean core subject and plural verb");
        assertFalse(optimized.contains("Learn how What Branch Policies Enforces works"), "Must not contain awkward question-in-hook phrasing");
        assertFalse(optimized.toLowerCase().contains("what what"), "Must never produce duplicate 'What what'");

        // 2. Domain-specific explanation and takeaway (Branch Policies, not Pipeline Caching)
        assertTrue(optimized.contains("enforcing mandatory code reviews, automating build validation, and protecting production branches"), "Explanation must match Branch Policies topic");
        assertFalse(optimized.contains("identifying pipeline bottlenecks"), "Must not leak pipeline bottleneck text into Branch Policies");
        assertTrue(optimized.contains("Enforcing branch policies on main and release branches"), "Key takeaway must match Branch Policies topic");
        assertFalse(optimized.contains("eliminate redundant downloads through caching"), "Must not leak pipeline caching takeaway into Branch Policies");

        // 3. No repetitive "best practices and techniques." spam in bullets
        assertFalse(optimized.contains("best practices and techniques."), "Must not spam 'best practices and techniques.' on every bullet");
        assertFalse(optimized.contains("For absolute beginners best practices"), "Must not produce broken bullet fragments");
        assertTrue(optimized.contains("- Configure minimum reviewer rules and approval policies for pull requests."));
        assertTrue(optimized.contains("- Enforce automated build validation policies to prevent broken merges."));

        // 4. Clean Target Audience without unrelated DBAs or trailing 'and interview preparation'
        assertFalse(optimized.contains("DBAs"), "Branch Policies audience should not include DBAs");
        assertFalse(optimized.contains("and interview preparation."), "Must not end with noun-mismatch 'and interview preparation.'");
        assertTrue(optimized.contains("engineers preparing for technical interviews."));

        // 5. Relevant bottom hashtags
        assertTrue(optimized.contains("#BranchPolicies"));
        assertTrue(optimized.contains("#AzureRepos"));
        assertTrue(optimized.contains("#BuildValidation"));
    }
}
