package com.technova.tagstudio.service;

import com.technova.tagstudio.dto.ChatMessageDTO;
import com.technova.tagstudio.dto.ChatRequestDTO;
import com.technova.tagstudio.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("${gemini.api.key:}")
    private String configuredApiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String model;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/openai/chat/completions}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // Cache of recent queries to prevent duplicate concurrent API calls
    private final ConcurrentHashMap<String, Long> recentQueries = new ConcurrentHashMap<>();

    public ChatResponseDTO processChat(ChatRequestDTO request) {
        List<ChatMessageDTO> messages = request.getMessages();
        if (messages == null || messages.isEmpty()) {
            return ChatResponseDTO.error("Message history cannot be empty.");
        }

        ChatMessageDTO lastMsg = messages.get(messages.size() - 1);
        String lastContent = lastMsg.getContent() != null ? lastMsg.getContent().trim() : "";
        if (lastContent.isEmpty()) {
            return ChatResponseDTO.error("Please enter a question.");
        }

        // Prevent rapid duplicate submissions (debounce / deduplication within 60 seconds)
        long now = System.currentTimeMillis();
        recentQueries.entrySet().removeIf(entry -> (now - entry.getValue()) >= 60000);
        if (recentQueries.size() >= 1000) {
            recentQueries.clear();
        }
        Long prevTime = recentQueries.get(lastContent.toLowerCase());
        if (prevTime != null && (now - prevTime) < 60000) {
            logger.info("Ignoring duplicate rapid request for: '{}'", lastContent);
            return ChatResponseDTO.error("Duplicate request detected. Please wait a moment before sending again.");
        }
        recentQueries.put(lastContent.toLowerCase(), now);

        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("GEMINI_API_KEY is not set in environment or properties.");
            return ChatResponseDTO.error(
                "Studio AI is not configured. Please set the GEMINI_API_KEY environment variable and restart the server.");
        }

        String rawTopic = request.getTopic() != null ? request.getTopic().trim() : "";
        String topic = cleanTopicString(rawTopic);
        String channel = request.getChannel() != null ? request.getChannel().trim() : "";
        List<String> selectedCategories = request.getSelectedCategories();

        logger.info("Calling Gemini ({}) for prompt: '{}' [topic='{}', categories={}]",
                model, lastContent, topic, selectedCategories != null ? selectedCategories.size() : 0);

        return callGeminiWithRetry(apiKey, messages, topic, channel, selectedCategories, 0);
    }

    /**
     * Executes the call to Gemini with safe single-retry for transient 429 rate-limits.
     * Quota exhaustion (insufficient_quota / RESOURCE_EXHAUSTED) is NEVER retried to prevent loops.
     */
    private ChatResponseDTO callGeminiWithRetry(String apiKey, List<ChatMessageDTO> messages,
                                                String topic, String channel,
                                                List<String> selectedCategories, int retryCount) {
        try {
            String requestBody = buildRequestBody(messages, topic, channel, selectedCategories);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body() != null ? response.body() : "";

            if (status == 200) {
                String reply = extractReply(responseBody);
                if (reply == null || reply.isBlank()) {
                    return new ChatResponseDTO("I received an empty response. Please rephrase your question.");
                }
                return new ChatResponseDTO(sanitizeReply(reply));
            }

            ErrorInfo errorInfo = parseError(responseBody);

            // Handle transient retryable errors: 503/5xx and transient 429 rate limit (not quota)
            boolean isTransient = (status == 503 || status == 502 || status == 504)
                    || (status == 429 && !isQuotaExhaustion(errorInfo));
            if (isTransient && retryCount < 2) {
                long backoffMs = 1500L * (1L << retryCount) + (long) (Math.random() * 500);
                logger.warn("[GEMINI_RETRY] Transient HTTP {} encountered (attempt {}/3). Backing off {}ms...",
                        status, retryCount + 1, backoffMs);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return callGeminiWithRetry(apiKey, messages, topic, channel, selectedCategories, retryCount + 1);
            }

            return formatErrorResponse(status, errorInfo);

        } catch (java.net.http.HttpTimeoutException e) {
            logger.error("Gemini request timed out", e);
            return ChatResponseDTO.error("Studio AI request timed out. Please try again.");
        } catch (Exception e) {
            logger.error("Error communicating with Gemini API", e);
            return ChatResponseDTO.error("Studio AI could not reach Google Gemini. Please check your internet connection.");
        }
    }

    /**
     * Resolves the API key securely.
     * Order:
     * 1. Spring properties / GEMINI_API_KEY environment variable
     * 2. System.getenv("GEMINI_API_KEY")
     * 3. Windows User Registry fallback (for keys set in Windows GUI without terminal restart)
     */
    private String resolveApiKey() {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey.trim();
        }

        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }

        // Fallback: check Windows HKCU\Environment registry if on Windows
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                Process p = Runtime.getRuntime().exec(new String[]{"reg", "query", "HKCU\\Environment", "/v", "GEMINI_API_KEY"});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.contains("GEMINI_API_KEY")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 3) {
                            return parts[2].trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Builds request JSON for Gemini Chat Completions (OpenAI-compatible format).
     * Limits history to the last 6 messages to keep token usage low (preventing TPM 429s).
     */
    public String buildRequestBody(List<ChatMessageDTO> messages, String topic, String channel, List<String> selectedCategories) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"").append(jsonEscape(model)).append("\",");
        sb.append("\"messages\":[");

        // System prompt
        StringBuilder sysPrompt = new StringBuilder();
        sysPrompt.append("You are Studio AI, a helpful, intelligent assistant inside TechNova Tag Studio. ")
                .append("Answer the user's CURRENT question directly, accurately, and naturally. ")
                .append("If the user asks casual questions (e.g., 'How are you?'), reply in a friendly conversational manner. ")
                .append("If the user asks technical or general questions (e.g., 'What is SQL Server?'), give a clear, informative explanation. ")
                .append("If the user asks for YouTube metadata, titles, descriptions, hashtags, or tags, provide creative suggestions");

        if (topic != null && !topic.isEmpty()) {
            sysPrompt.append(" focused on the topic: ").append(topic);
        }
        if (channel != null && !channel.isEmpty()) {
            sysPrompt.append(" for channel: ").append(channel);
        }
        sysPrompt.append(". ");

        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            sysPrompt.append("Active categories selected in the workspace: ")
                    .append(String.join(", ", selectedCategories))
                    .append(". Contextually incorporate these category angles and keywords into recommendations when relevant. ");
        }

        sysPrompt.append("When generating YouTube titles, format the topic as clean natural text (e.g., 'SQL Server Performance Tuning') under 100 characters. Never output labels like 'The Topic:', 'Topic:', or markdown bold asterisks (**) around the topic or title. ");
        sysPrompt.append("When generating YouTube descriptions, video copy, or metadata recommendations, always include a dedicated Call to Action (CTA) section encouraging viewers to like the video, subscribe to ");
        if (channel != null && !channel.isEmpty()) {
            sysPrompt.append(channel);
        } else {
            sysPrompt.append("the channel");
        }
        sysPrompt.append(", and drop comments. ");
        sysPrompt.append("Format descriptions following the professional ChatGPT-style 9-part structure: ")
                .append("1. Opening Hook with natural emojis and concise value proposition; ")
                .append("2. Topic Explanation in 1-2 readable paragraphs covering core technical concepts; ")
                .append("3. What You'll Learn with header '🔹 **What you'll learn:**' followed by bullet points on separate lines; ")
                .append("4. Key Takeaway starting with '**Key takeaway:** '; ")
                .append("5. Target Audience stating relevant professionals cleanly (e.g., '**Target audience:** DevOps engineers, cloud architects, and developers'); ")
                .append("6. Engagement question CTA starting with '💬 '; ")
                .append("7. Like & subscribe CTA starting with '👍 Like this video and subscribe to ")
                .append(channel != null && !channel.isEmpty() ? channel : "TechNova IQ")
                .append("...'; ")
                .append("8. Brand closing line: '")
                .append(channel != null && !channel.isEmpty() ? channel : "TechNova IQ")
                .append(" — Learn Smarter. Code Faster.'; ")
                .append("9. Relevant hashtags (5-10 concise, recognizable, topic-specific tags at the bottom). ");
        sysPrompt.append("STRICT SEO QUALITY & ANTI-STUFFING: Identify ONE primary keyword and integrate secondary keywords naturally. Strictly eliminate keyword stuffing, keyword dumping, and repeating the primary keyword across every sentence or bullet point. Eliminate artificial keyword variations (e.g. topic + basics, topic + tutorial, topic + 2026, topic + full course free, topic + for absolute beginners) and unrelated category leakage (e.g. database design, clean code) unless directly relevant. Never invent course, free-content, year, or beginner claims unsupported by the video. ");
        sysPrompt.append("STRICT OUTPUT CLEANLINESS: The generated description must contain ONLY publish-ready content. Never include internal notes, NOTE sections, GSD phase references, plan numbers, milestone or execution references, testing instructions, acceptance criteria, assistant conversational messages, or follow-up questions asking for user approval. ");
        sysPrompt.append("Never output hardcoded or repetitive responses. Always answer the specific question asked.");

        sb.append("{\"role\":\"system\",\"content\":\"").append(jsonEscape(sysPrompt.toString())).append("\"}");

        // Include up to last 6 messages to preserve context without exceeding token quotas
        int startIdx = Math.max(0, messages.size() - 6);
        for (int i = startIdx; i < messages.size(); i++) {
            ChatMessageDTO msg = messages.get(i);
            String role = msg.getRole() != null ? msg.getRole() : "user";
            String content = msg.getContent() != null ? msg.getContent() : "";
            sb.append(",{\"role\":\"").append(jsonEscape(role))
              .append("\",\"content\":\"").append(jsonEscape(content)).append("\"}");
        }

        sb.append("],");
        sb.append("\"max_tokens\":500,");
        sb.append("\"temperature\":0.7");
        sb.append("}");

        return sb.toString();
    }

    private String extractReply(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                return contentNode.asText().trim();
            }
        } catch (Exception ignored) {
        }

        String marker = "\"content\":";
        int idx = json.indexOf(marker);
        if (idx == -1) return null;
        int start = json.indexOf('"', idx + marker.length()) + 1;
        if (start == 0) return null;

        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                switch (c) {
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    case '/' -> result.append('/');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    default -> result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    /**
     * Optimizes raw YouTube descriptions into a clean, human-readable 9-part ChatGPT structure.
     * Eliminates keyword dumps, removes redundant keyword prefixes, filters out unrelated categories
     * and ungrounded claims (e.g. "2026", "full course free"), and curates hashtags into concise, recognizable tags.
     */
    public String optimizeDescription(String rawDescription, String topic, String channel) {
        if (rawDescription == null || rawDescription.trim().isEmpty()) {
            return "";
        }

        String ch = (channel != null && !channel.isBlank()) ? channel.trim() : "TechNova IQ";
        String primaryTopic = (topic != null && !topic.isBlank()) ? cleanTopicString(topic) : "Azure DevOps Pipeline Optimization";

        // Extract key components from raw template if present
        String[] paras = rawDescription.split("\n\n+");

        String hook = "";
        String keywordsRaw = "";
        String audienceRaw = "";
        String question = "";
        String closingCta = "";

        for (String p : paras) {
            String trimmed = p.trim();
            if (trimmed.startsWith("Learn how") || trimmed.startsWith("Is your") || trimmed.startsWith("Discover how")
                    || (hook.isEmpty() && !trimmed.contains("walkthrough, you'll explore") && !trimmed.contains("Perfect for") && !trimmed.startsWith("#") && !trimmed.contains("— Learn Smarter"))) {
                hook = trimmed;
            } else if (trimmed.contains("walkthrough, you'll explore") || trimmed.contains("explore ") || trimmed.contains("master essential techniques")) {
                keywordsRaw = trimmed;
            } else if (trimmed.startsWith("Perfect for") || trimmed.toLowerCase().contains("beginners, developers")) {
                audienceRaw = trimmed;
            } else if (trimmed.startsWith("What ") || trimmed.contains("Share below") || trimmed.contains("Which stage takes the longest")) {
                question = trimmed;
            } else if (trimmed.startsWith("If this helped") || trimmed.startsWith("Like this video") || trimmed.contains("subscribe to")) {
                closingCta = trimmed;
            }
        }

        if (hook.isEmpty()) {
            hook = "Master " + primaryTopic + " with practical, real-world walkthroughs from " + ch + " to accelerate your build performance and engineering workflow! 🚀";
        }

        // Section 2: Topic Explanation
        String explanation = "In this video, learn practical approaches to " + primaryTopic + ", focusing on identifying pipeline bottlenecks, optimizing dependencies, and streamlining execution for reliable continuous integration.";

        // Section 3: What You'll Learn (curate from keywordsRaw without repetitive prefixes or ungrounded claims)
        List<String> bullets = new ArrayList<>();
        if (!keywordsRaw.isEmpty()) {
            // Strip wrapper prefix and suffix
            String itemsOnly = keywordsRaw.replaceAll("(?i)^.*?explore\\s+", "")
                                          .replaceAll("(?i),\\s*helping you master.*$", "");
            String[] rawItems = itemsOnly.split(",\\s*");
            Set<String> seen = new LinkedHashSet<>();
            for (String item : rawItems) {
                String cleanItem = item.trim().replaceAll("(?i)^" + Pattern.quote(primaryTopic) + "\\s*", "").trim();
                // Filter out ungrounded claims and unrelated category leakage
                if (cleanItem.isEmpty() || cleanItem.matches("(?i).*(?:2026|full course free|basics|tutorial|explained).*")
                        || cleanItem.equalsIgnoreCase("database design")
                        || cleanItem.equalsIgnoreCase("database normalization")
                        || cleanItem.equalsIgnoreCase("clean code")
                        || cleanItem.equalsIgnoreCase("coding best practices")
                        || cleanItem.equalsIgnoreCase("YouTube Shorts")
                        || cleanItem.equalsIgnoreCase("Shorts tutorial")) {
                    continue;
                }
                String bulletText = capitalizeWords(cleanItem);
                if (seen.add(bulletText.toLowerCase())) {
                    bullets.add("- " + bulletText + " best practices and techniques.");
                    if (bullets.size() >= 5) break;
                }
            }
        }
        if (bullets.isEmpty()) {
            bullets.add("- Identify and resolve pipeline bottlenecks efficiently.");
            bullets.add("- Configure dependency caching to eliminate repeated downloads.");
            bullets.add("- Parallelize independent jobs across available agent capacity.");
            bullets.add("- Measure performance benchmarks before and after optimization.");
        }

        // Section 4: Key Takeaway
        String keyTakeaway = "**Key takeaway:** Measure pipeline performance first, eliminate redundant downloads through caching, and execute independent jobs in parallel for maximum build efficiency.";

        // Section 5: Target Audience
        String targetAudience;
        if (!audienceRaw.isBlank()) {
            String cleanAudience = audienceRaw
                    .replaceAll("(?i)^(Perfect for|Whether you are a|Whether you're a)\\s*", "")
                    .replaceAll("(?i)(,\\s*this breakdown is designed for you\\.?|,\\s*this guide is for you\\.?|\\.?)$", "")
                    .trim();
            if (cleanAudience.toLowerCase().startsWith(primaryTopic.toLowerCase())) {
                cleanAudience = cleanAudience.substring(primaryTopic.length()).trim();
                cleanAudience = cleanAudience.replaceAll("^[\\s,]+", "");
            }
            if (cleanAudience.isEmpty()) {
                targetAudience = "**Target audience:** Developers, DevOps engineers, cloud architects, and technical interview candidates.";
            } else {
                cleanAudience = Character.toUpperCase(cleanAudience.charAt(0)) + cleanAudience.substring(1);
                targetAudience = "**Target audience:** " + cleanAudience + (cleanAudience.endsWith(".") ? "" : ".");
            }
        } else {
            targetAudience = "**Target audience:** Developers, DevOps engineers, cloud architects, and technical interview candidates.";
        }

        // Section 6: Engagement CTA
        if (question.isEmpty()) {
            question = "💬 Which pipeline stage takes the longest in your workflow: Restore, Build, or Test? Share your experience in the comments below!";
        } else if (!question.startsWith("💬")) {
            question = "💬 " + question;
        }

        // Section 7: Like & Subscribe CTA
        if (closingCta.isEmpty()) {
            closingCta = "👍 Like this video and subscribe to **" + ch + "** for practical DevOps tutorials, cloud engineering, and technical interview preparation.";
        } else if (!closingCta.startsWith("👍")) {
            closingCta = "👍 " + closingCta;
        }

        // Section 8: Brand Closing
        String brandClosing = ch + " — Learn Smarter. Code Faster.";

        // Section 9: Concise Hashtags (5-10 concise tags)
        Set<String> hashtags = new LinkedHashSet<>();
        hashtags.add("#" + primaryTopic.replaceAll("[^a-zA-Z0-9]", ""));
        hashtags.add("#CICD");
        hashtags.add("#DevOps");
        hashtags.add("#PipelineOptimization");
        hashtags.add("#CloudEngineering");
        hashtags.add("#" + ch.replaceAll("[^a-zA-Z0-9]", ""));
        hashtags.add("#Shorts");
        String hashtagLine = String.join(" ", hashtags);

        // Build 9-part structured output
        StringBuilder sb = new StringBuilder();
        sb.append(hook).append("\n\n");
        sb.append(explanation).append("\n\n");
        sb.append("🔹 **What you'll learn:**\n");
        for (String b : bullets) {
            sb.append(b).append("\n");
        }
        sb.append("\n");
        sb.append(keyTakeaway).append("\n\n");
        sb.append(targetAudience).append("\n\n");
        sb.append(question).append("\n\n");
        sb.append(closingCta).append("\n\n");
        sb.append(brandClosing).append("\n\n");
        sb.append(hashtagLine);

        return sb.toString().trim();
    }

    public ErrorInfo parseError(String json) {
        ErrorInfo info = new ErrorInfo();
        if (json == null || json.isEmpty()) return info;

        try {
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode errNode = root.path("error");
            if (errNode.isMissingNode() && root.isArray() && root.size() > 0) {
                errNode = root.get(0).path("error");
            }
            if (!errNode.isMissingNode()) {
                info.code = errNode.path("code").asText(null);
                info.type = !errNode.path("status").isMissingNode() ? errNode.path("status").asText(null) : errNode.path("type").asText(null);
                info.message = errNode.path("message").asText(null);
                return info;
            }
        } catch (Exception ignored) {
        }

        info.code = extractJsonField(json, "\"code\":");
        info.type = extractJsonField(json, "\"type\":");
        info.message = extractJsonField(json, "\"message\":");
        return info;
    }

    private String extractJsonField(String json, String fieldPattern) {
        int idx = json.indexOf(fieldPattern);
        if (idx == -1) return null;
        int valStart = idx + fieldPattern.length();
        // skip whitespace
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }
        if (valStart >= json.length()) return null;

        if (json.charAt(valStart) == '"') {
            int end = json.indexOf('"', valStart + 1);
            if (end != -1) {
                return json.substring(valStart + 1, end);
            }
        } else {
            // null or number
            int end = valStart;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            String raw = json.substring(valStart, end).trim();
            return "null".equals(raw) ? null : raw;
        }
        return null;
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String cleanTopicString(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String s = str.trim();
        // Remove emoji / decorative symbols
        s = s.replaceAll("[\\p{So}\\p{Sk}📌*#_~`\"']", " ").trim();
        // Remove "The Topic:", "Topic:", "The topic", "Topic" prefixes
        s = s.replaceAll("(?i)^(?:the\\s+|my\\s+)?topic\\s*(?:is|:|=|-|\\s)\\s*", "");
        s = s.replaceAll("(?i)^(?:the\\s+)?topic\\b\\s*", "");
        s = s.replaceAll("[*#_~`\"':\\-]", " ").trim();
        // Remove trailing instructional noise
        s = s.replaceAll("(?i)\\s+(?:please|and\\s+focus|focusing|focused|with|in\\s+2026).*$", "").trim();
        s = s.replaceAll("\\s+", " ").trim();
        return capitalizeWords(s);
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return "";
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                if (w.equalsIgnoreCase("devops")) {
                    sb.append("DevOps");
                } else if (w.equalsIgnoreCase("sql")) {
                    sb.append("SQL");
                } else if (w.equalsIgnoreCase("api")) {
                    sb.append("API");
                } else if (w.equalsIgnoreCase("aws")) {
                    sb.append("AWS");
                } else {
                    sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
                }
            }
        }
        return sb.toString();
    }

    public String sanitizeReply(String reply) {
        if (reply == null) return null;
        // Strip accidental "The Topic: **...**" or "The Topic: ..." from title prefixes or headers
        String cleaned = reply.replaceAll("(?i)(?:^|(?<=\\n))\\s*(?:[#*-]+\\s*)?(?:the\\s+)?topic\\s*[:=]\\s*\\*\\*([^*]+)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("(?i)(?:^|(?<=\\n))\\s*(?:[#*-]+\\s*)?(?:the\\s+)?topic\\s*[:=]\\s*", "");
        cleaned = cleaned.replaceAll("(?i)\\b(?:the\\s+)?topic\\s*[:=]\\s*\\*\\*([^*]+)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("(?i)\\b(?:the\\s+)?topic\\s*[:=]\\s*", "");
        return cleaned.trim();
    }

    /**
     * Formats YouTube descriptions for clean display and Shorts compatibility.
     * Decodes HTML entities safely, strips unwanted HTML tags and markdown header hashes,
     * strips markdown bolding while preserving bullet points and emojis, and prevents
     * YouTube whitespace collapsing by inserting zero-width spaces (\u200B) on blank lines.
     */
    public String formatYouTubeDescription(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "";
        }

        String text = rawText;

        // Strip "Studio AI" / "Studio AI:" prefix if present
        text = text.replaceAll("(?i)^\\s*(?:<strong>)?Studio AI(?:</strong>)?\\s*[:\\-]?\\s*", "");

        // Convert literal \n or \r escape sequences to real newlines
        text = text.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\r", "\n");

        // Convert HTML break tags to newlines
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");

        // Decode common HTML entities safely without corrupting legitimate special characters
        text = text.replace("&amp;", "&")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&nbsp;", " ")
                   .replace("&#x20;", " ")
                   .replace("&#32;", " ");

        // Strip HTML tags (e.g., <strong>, </strong>, <p>, </p>, <div>, etc.)
        text = text.replaceAll("<[^>]+>", "");

        // Strip Markdown headers (# Title, ## Description, etc.) if they appear as standalone header lines
        text = text.replaceAll("(?m)^#{1,6}\\s+(?:Title|Description)?\\s*", "");

        // Strip markdown bolding **text** -> text, while preserving list bullet asterisks (* bullet)
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // Normalize line breaks to \n
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // YouTube Shorts whitespace collapsing fix:
        // YouTube description box collapses empty lines (consecutive newlines).
        // By replacing each empty or whitespace-only line with an invisible Unicode Zero-Width Space (\u200B),
        // YouTube preserves all paragraph breaks and blank lines exactly as intended.
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) {
                result.append("\u200B");
            } else {
                result.append(line.trim());
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString().trim();
    }

    public ChatResponseDTO formatErrorResponse(int status, ErrorInfo errorInfo) {
        if (errorInfo == null) {
            errorInfo = new ErrorInfo();
        }

        if (status == 401 || status == 403) {
            logger.error("[GEMINI_AUTH_ERROR] HTTP {}: Authentication failed or invalid API key. code={}, type={}, message={}",
                    status, errorInfo.code, errorInfo.type, errorInfo.message);
            return ChatResponseDTO.error("Studio AI: Authentication failed. Please check your GEMINI_API_KEY.");
        }

        if (status == 404) {
            logger.error("[GEMINI_NOT_FOUND] HTTP 404: Model or endpoint not found. Model: '{}', URL: '{}', message: '{}'",
                    model, apiUrl, errorInfo.message);
            return ChatResponseDTO.error("Studio AI: Model or endpoint not found (" + model + "). Please verify configuration.");
        }

        if (status == 400) {
            logger.error("[GEMINI_BAD_REQUEST] HTTP 400: Bad request or invalid payload. code={}, type={}, message={}",
                    errorInfo.code, errorInfo.type, errorInfo.message);
            String detail = (errorInfo.message != null && !errorInfo.message.isBlank()) ? errorInfo.message : "Bad Request";
            return ChatResponseDTO.error("Studio AI: Invalid request parameters (" + detail + ").");
        }

        if (status == 429) {
            boolean isQuota = isQuotaExhaustion(errorInfo);
            if (isQuota) {
                logger.error("[GEMINI_QUOTA_EXHAUSTED] HTTP 429: Account has insufficient quota/credits. code={}, type={}, message={}",
                        errorInfo.code, errorInfo.type, errorInfo.message);
                return ChatResponseDTO.error(
                        "Gemini Quota Exceeded (429): Your Gemini API quota has been reached (code: " +
                                (errorInfo.code != null ? errorInfo.code : "insufficient_quota") +
                                "). Please verify your quota in Google AI Studio to enable Studio AI.");
            } else {
                logger.warn("[GEMINI_RATE_LIMIT] HTTP 429: Rate limit reached. code={}, type={}, message={}",
                        errorInfo.code, errorInfo.type, errorInfo.message);
                return ChatResponseDTO.error(
                        "Studio AI Rate Limit (429): Request limit reached for " + model + ". Please wait a few seconds and try again.");
            }
        }

        if (status == 503 || status == 502 || status == 504 || status == 500) {
            logger.error("[GEMINI_SERVICE_UNAVAILABLE] HTTP {}: Google Gemini service unavailable. code={}, type={}, message={}",
                    status, errorInfo.code, errorInfo.type, errorInfo.message);
            return ChatResponseDTO.error(
                    "Studio AI: Google Gemini is temporarily experiencing high demand (HTTP " + status + ": " + model + "). Please try again in a moment.");
        }

        logger.error("[GEMINI_HTTP_ERROR] HTTP {}: Unexpected response from Gemini. code={}, type={}, message={}",
                status, errorInfo.code, errorInfo.type, errorInfo.message);
        return ChatResponseDTO.error("Studio AI: Service temporarily unavailable (HTTP " + status + "). Please try again.");
    }

    public boolean isQuotaExhaustion(ErrorInfo errorInfo) {
        if (errorInfo == null) return false;
        return "insufficient_quota".equalsIgnoreCase(errorInfo.code)
                || "credit_balance_exhausted".equalsIgnoreCase(errorInfo.code)
                || "RESOURCE_EXHAUSTED".equalsIgnoreCase(errorInfo.code)
                || "insufficient_quota".equalsIgnoreCase(errorInfo.type)
                || (errorInfo.message != null && errorInfo.message.toLowerCase().contains("credit"))
                || (errorInfo.message != null && errorInfo.message.toLowerCase().contains("quota"))
                || (errorInfo.message != null && errorInfo.message.toLowerCase().contains("exhausted"))
                || (errorInfo.message != null && errorInfo.message.toLowerCase().contains("billing"));
    }

    public static class ErrorInfo {
        public String code;
        public String type;
        public String message;

        public ErrorInfo() {}

        public ErrorInfo(String code, String type, String message) {
            this.code = code;
            this.type = type;
            this.message = message;
        }
    }
}
