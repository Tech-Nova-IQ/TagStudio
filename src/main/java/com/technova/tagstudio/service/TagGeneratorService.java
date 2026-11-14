package com.technova.tagstudio.service;

import com.technova.tagstudio.dto.*;
import com.technova.tagstudio.model.TagCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(TagGeneratorService.class);

    private static final String DEFAULT_CHANNEL = "TechNova IQ";
    private static final String DEFAULT_TOPIC = "SQL Server Performance Tuning";

    private static final List<String> CHANNEL_TEMPLATES = List.of(
            "{ch}", "{ch} channel", "{ch} tutorials", "{ch} sql server tutorials", "{ch} tech content",
            "youtubeshorts", "shorts", "viral", "trending", "newvideo", "videooftheday", "contentcreator",
            "creator", "subscribe", "watchnow", "youtuber", "explore", "fyp", "foryou", "mustwatch",
            "goviral", "likeandsubscribe", "viralcontent", "newtrend", "techshorts", "techreview",
            "techtips", "techtrends", "technology", "machinelearning"
    );

    private static final List<String> TOPIC_TEMPLATES = List.of(
            "{tool} tutorial", "{tool} basics", "{tool} explained", "{tool} overview", "{tool} complete guide",
            "{tool} full course", "{tool} crash course", "{tool} roadmap", "{tool} for beginners", "{tool} advanced concepts",
            "{tool} deep dive", "{tool} architecture", "{tool} fundamentals", "{tool} hands on practice", "{tool} practical guide",
            "{tool} real world examples", "{tool} use cases", "{tool} best practices", "{tool} tips and tricks", "{tool} common mistakes",
            "{tool} cheat sheet", "{tool} interview questions", "{tool} certification guide", "{tool} career path", "{tool} jobs",
            "{tool} salary", "{tool} trends 2026", "{tool} learning path", "{tool} vs alternatives", "{tool} getting started"
    );

    private static final List<String> DATABASE_TEMPLATES = List.of(
            "database design", "database normalization", "acid properties", "indexing strategies", "query optimization",
            "backup strategy", "disaster recovery", "high availability", "database replication", "database sharding",
            "data modeling", "er diagrams", "database security", "access control", "database migration",
            "etl process", "data warehousing", "sql vs nosql", "database administration", "capacity planning",
            "database monitoring", "transaction isolation", "database performance tuning", "schema design", "database certification",
            "dba career path", "dba salary", "dba interview questions", "database jobs", "database trends 2026"
    );

    private static final List<String> DEVOPS_TEMPLATES = List.of(
            "ci cd pipeline", "infrastructure as code", "containerization", "docker basics", "kubernetes basics",
            "terraform", "ansible", "configuration management", "gitops", "continuous deployment",
            "continuous integration", "automated testing", "monitoring and alerting", "site reliability engineering", "cloud migration",
            "microservices", "version control", "git workflows", "deployment automation", "blue green deployment",
            "canary release", "devops culture", "devops tools", "devops roadmap", "devops certification",
            "devops interview questions", "devops career path", "devops salary", "devops jobs", "devops trends 2026"
    );

    private static final List<String> MONITORING_TEMPLATES = List.of(
            "performance monitoring", "query performance tuning", "execution plan analysis", "index tuning", "wait statistics",
            "deadlock detection", "blocking analysis", "resource utilization", "cpu and memory monitoring", "disk io monitoring",
            "log monitoring", "alerting systems", "dashboards", "observability", "apm tools",
            "database health checks", "slow query analysis", "capacity planning", "monitoring best practices", "monitoring tools",
            "uptime monitoring", "latency monitoring", "error tracking", "monitoring automation", "real time monitoring",
            "monitoring interview questions", "monitoring career", "sre monitoring", "monitoring certification", "monitoring trends 2026"
    );

    private static final List<String> PROGRAMMING_TEMPLATES = List.of(
            "clean code", "coding best practices", "software design patterns", "object oriented programming", "data structures and algorithms",
            "debugging techniques", "code review", "version control with git", "unit testing", "test driven development",
            "software architecture", "api development", "backend development", "programming interview questions", "coding challenges",
            "competitive programming", "programming languages comparison", "software engineering practices", "code optimization", "refactoring",
            "programming career", "coding bootcamp", "programming certification", "programming jobs", "programming salary",
            "programming roadmap", "programming tips", "programming for beginners", "programming trends 2026", "programming portfolio"
    );

    private static final List<String> SHORTS_TEMPLATES = List.of(
            "youtube shorts", "shorts tutorial", "quick tips", "sixty second tutorial", "coding shorts",
            "tech shorts", "shorts for beginners", "shorts explained", "viral shorts", "shorts script ideas",
            "shorts hooks", "shorts editing tips", "shorts thumbnail ideas", "shorts trends 2026", "shorts algorithm",
            "shorts vs long form", "shorts monetization", "shorts growth tips", "shorts content ideas", "shorts hashtags",
            "shorts seo", "shorts watch time", "shorts retention", "shorts call to action", "shorts niche ideas",
            "shorts for tech channel", "shorts scripting", "shorts b roll ideas", "shorts posting schedule", "shorts analytics"
    );

    private static final List<String> LOWCOMP_TEMPLATES = List.of(
            "{tool} for absolute beginners", "{tool} common errors explained", "{tool} step by step guide", "{tool} project for resume", "{tool} mini project ideas",
            "{tool} comparison guide", "{tool} troubleshooting guide", "{tool} setup on windows", "{tool} setup on linux", "{tool} configuration walkthrough",
            "{tool} real interview questions", "{tool} practical examples", "{tool} for freshers", "{tool} learning path", "{tool} common doubts clarified",
            "{tool} hands on exercises", "{tool} exercises with solutions", "{tool} notes pdf", "{tool} cheat sheet download", "{tool} short course",
            "{tool} weekend project", "{tool} portfolio project", "{tool} case study", "{tool} for career switchers", "{tool} without coding background",
            "{tool} self study plan", "{tool} free resources", "{tool} certification worth it", "{tool} salary in india", "{tool} job openings 2026"
    );

    private static final List<String> HIDDEN_TEMPLATES = List.of(
            "{tool} dba jobs", "{tool} career path", "{tool} community", "{tool} discord server", "{tool} reddit community",
            "{tool} forum discussions", "{tool} user group", "{tool} local meetup", "{tool} conference talks", "{tool} podcast recommendations",
            "{tool} newsletter", "{tool} blog recommendations", "{tool} github repositories", "{tool} open source contribution", "{tool} mentorship programs",
            "{tool} freelance gigs", "{tool} remote jobs", "{tool} contract roles", "{tool} niche use cases", "{tool} lesser known features",
            "{tool} hidden gems", "{tool} underrated tips", "{tool} advanced tricks", "{tool} internal tooling", "{tool} enterprise use cases",
            "{tool} startup use case", "{tool} government sector jobs", "{tool} banking sector jobs", "{tool} healthcare sector use", "{tool} interview red flags"
    );

    private static final List<String> SEO_TEMPLATES = List.of(
            "{tool} 2026", "{tool} tutorial for beginners 2026", "{tool} full course free", "{tool} crash course", "{tool} roadmap 2026",
            "{tool} interview questions and answers", "{tool} certification guide", "{tool} salary in india 2026", "{tool} career guide", "{tool} vs alternatives 2026",
            "{tool} latest features", "{tool} whats new", "{tool} best practices 2026", "{tool} tips for professionals", "{tool} for resume",
            "{tool} skills to learn", "{tool} learning roadmap", "{tool} free course", "{tool} paid course worth it", "{tool} youtube channel recommendations",
            "{tool} book recommendations", "{tool} documentation guide", "{tool} official docs explained", "{tool} community resources", "{tool} trending topics",
            "{tool} future scope", "{tool} job market 2026", "{tool} demand in india", "{tool} top companies using", "{tool} real world projects"
    );

    private static final List<String> CUSTOM_CORE_TEMPLATES = List.of(
            "{tool} configuration", "{tool} architecture deep dive", "{tool} performance tuning", "{tool} security best practices", "{tool} common commands",
            "{tool} advanced features", "{tool} integration with other tools", "{tool} troubleshooting common issues", "{tool} version differences", "{tool} migration guide",
            "{tool} installation guide", "{tool} monitoring setup", "{tool} automation scripts", "{tool} api usage", "{tool} plugin ecosystem",
            "{tool} configuration files explained", "{tool} logging and debugging", "{tool} scaling strategies", "{tool} backup strategy", "{tool} disaster recovery",
            "{tool} cost optimization", "{tool} cloud deployment", "{tool} on premise setup", "{tool} cli commands", "{tool} gui walkthrough",
            "{tool} common pitfalls", "{tool} production checklist", "{tool} testing strategy", "{tool} documentation deep dive", "{tool} community best practices"
    );

    public TagStudioResponseDTO generateStudioData(TagStudioRequestDTO request) {
        logger.info("Generating studio data for topic: '{}', channel: '{}'", request.getVideoTopic(), request.getChannelName());

        String channelName = (request.getChannelName() != null && !request.getChannelName().trim().isEmpty())
                ? request.getChannelName().trim() : DEFAULT_CHANNEL;
        
        String prompt = request.getPrompt();
        String extractedTopic = extractTopicFromPrompt(prompt);
        String videoTopic;
        if (extractedTopic != null && !extractedTopic.trim().isEmpty()) {
            videoTopic = extractedTopic.trim();
        } else if (request.getVideoTopic() != null && !request.getVideoTopic().trim().isEmpty()) {
            videoTopic = request.getVideoTopic().trim();
        } else {
            videoTopic = DEFAULT_TOPIC;
        }
        videoTopic = cleanTopicString(videoTopic);
        if (videoTopic.isEmpty()) {
            videoTopic = DEFAULT_TOPIC;
        }

        List<String> activeCategoryKeys = request.getSelectedCategories();
        if (activeCategoryKeys == null || activeCategoryKeys.isEmpty()) {
            activeCategoryKeys = new ArrayList<>();
            for (TagCategory category : TagCategory.values()) {
                activeCategoryKeys.add(category.getKey());
            }
        }

        String topicSubject = extractShortSubject(videoTopic, 4).toLowerCase();
        String coreLabel = capitalizeWords(extractShortSubject(videoTopic, 3));
        if (coreLabel.isEmpty()) coreLabel = "Core";

        Map<String, List<String>> categoryRawMap = new LinkedHashMap<>();
        categoryRawMap.put("channel", padTo30(CHANNEL_TEMPLATES.stream().map(t -> t.replace("{ch}", channelName.toLowerCase())).toList()));
        categoryRawMap.put("topic", padTo30(TOPIC_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList()));
        categoryRawMap.put("core", padTo30(CUSTOM_CORE_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList()));
        categoryRawMap.put("database", padTo30(DATABASE_TEMPLATES));
        categoryRawMap.put("devops", padTo30(DEVOPS_TEMPLATES));
        categoryRawMap.put("monitoring", padTo30(MONITORING_TEMPLATES));
        categoryRawMap.put("programming", padTo30(PROGRAMMING_TEMPLATES));
        categoryRawMap.put("shorts", padTo30(SHORTS_TEMPLATES));
        categoryRawMap.put("lowcomp", padTo30(LOWCOMP_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList()));
        categoryRawMap.put("hidden", padTo30(HIDDEN_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList()));
        categoryRawMap.put("seo", padTo30(SEO_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList()));

        List<CategoryDataDTO> categories = new ArrayList<>();
        int totalItems = 0;

        for (TagCategory categoryEnum : TagCategory.values()) {
            if (activeCategoryKeys.contains(categoryEnum.getKey())) {
                List<String> rawPhrases = categoryRawMap.get(categoryEnum.getKey());
                List<String> hashtags = rawPhrases.stream().map(this::toHashtag).collect(Collectors.toList());

                String label = categoryEnum.getKey().equals("core") ? coreLabel : categoryEnum.getDefaultLabel();

                CategoryDataDTO catDTO = new CategoryDataDTO(
                        categoryEnum.getKey(),
                        label,
                        categoryEnum.getColor(),
                        rawPhrases,
                        hashtags,
                        rawPhrases
                );
                categories.add(catDTO);
                totalItems += rawPhrases.size();
            }
        }

        MetadataDTO metadata = buildMetadata(channelName, videoTopic, topicSubject, categoryRawMap, activeCategoryKeys, coreLabel, prompt);
        PlaylistMetadataDTO playlistMetadata = buildPlaylistMetadata(channelName, videoTopic, topicSubject, categoryRawMap, prompt);

        TagStudioResponseDTO response = new TagStudioResponseDTO();
        response.setChannelName(channelName);
        response.setVideoTopic(videoTopic);
        response.setCoreLabel(coreLabel);
        response.setTotalCategories(categories.size());
        response.setTotalItems(totalItems);
        response.setCategories(categories);
        response.setMetadata(metadata);
        response.setPlaylistMetadata(playlistMetadata);

        return response;
    }

    private MetadataDTO buildMetadata(String channel, String topic, String topicSubject,
                                       Map<String, List<String>> categoryRawMap,
                                       List<String> activeCategoryKeys, String coreLabel,
                                       String prompt) {
        List<String> titleOptions = new ArrayList<>();
        String topicTitle = capitalizeWords(topic);

        // Preferred structure: [Topic/Concept] Explained in [Technology/Platform] #Shorts
        String preferredTitle;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(.*?)\\s+(in|on|for|with)\\s+(.*)$", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(topicTitle);
        if (m.matches()) {
            String concept = capitalizeWords(m.group(1).trim());
            String prep = m.group(2).toLowerCase();
            String tech = capitalizeWords(m.group(3).trim());
            preferredTitle = concept + " Explained " + prep + " " + tech + " #Shorts";
        } else {
            preferredTitle = topicTitle + " Explained #Shorts";
        }

        String opt1 = formatSeoTitle(preferredTitle);
        String opt2 = formatSeoTitle(topicTitle + " #Shorts");

        String opt3;
        if (prompt != null && !prompt.trim().isEmpty()) {
            String instruction = prompt.trim()
                    .replaceAll("(?i)^(?:the\\s+|my\\s+)?topic\\s*(?:is|:|=|-|\\s)[^.\\n\\r,;]+[.,;]?\\s*", "")
                    .replaceAll("(?i)^(?:create|generate|provide|write|make)\\s+(?:the\\s+)?(?:youtube\\s+)?(?:metadata|tags|keywords|titles|hashtags|description|content|video)\\s+(?:for|about|on)\\s+(?:(?:the\\s+)?topic\\s*[:=]?\\s*)?[^.\\n\\r,;]+[.,;]?\\s*", "")
                    .replaceAll("(?i)^(?:please\\s+)?(?:provide|generate|create)\\s+(?:the\\s+)?(?:youtube\\s+)?(?:metadata|tags)[.,;]?\\s*", "")
                    .replaceAll("[\\r\\n]+", " ")
                    .trim();
            if (!instruction.isEmpty() && instruction.length() < 50 && !instruction.equalsIgnoreCase(topic)) {
                opt3 = formatSeoTitle(topicTitle + " — " + capitalizeWords(instruction) + " #Shorts");
            } else {
                opt3 = formatSeoTitle(topicTitle + " Explained | " + channel + " #Shorts");
            }
        } else {
            opt3 = formatSeoTitle(topicTitle + " Explained | " + channel + " #Shorts");
        }

        titleOptions.add(opt1);
        if (!opt2.equalsIgnoreCase(opt1)) titleOptions.add(opt2);
        if (!opt3.equalsIgnoreCase(opt1) && !opt3.equalsIgnoreCase(opt2)) titleOptions.add(opt3);
        if (titleOptions.size() < 3) {
            String fallback = formatSeoTitle(topicTitle + " Tutorial #Shorts");
            if (!titleOptions.contains(fallback)) titleOptions.add(fallback);
        }

        List<String> topicWords = TOPIC_TEMPLATES.stream().map(t -> fillTool(t, topic.toLowerCase())).toList();
        List<String> topicWordsForHash = TOPIC_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList();

        // 1. Collect mixed SEO keywords from the current topic and selected categories (no headings, natural text)
        Set<String> seenKeywords = new HashSet<>();
        List<String> mixedKeywords = new ArrayList<>();

        List<String> priorityOrder = List.of("core", "topic", "seo", "lowcomp");
        for (String key : priorityOrder) {
            if (activeCategoryKeys.contains(key) && categoryRawMap.containsKey(key)) {
                int added = 0;
                for (String item : categoryRawMap.get(key)) {
                    if (item != null && !item.isBlank() && seenKeywords.add(item.toLowerCase().trim())) {
                        mixedKeywords.add(item.trim());
                        added++;
                        if (added >= 3) break;
                    }
                }
            }
        }

        for (String key : activeCategoryKeys) {
            if (!priorityOrder.contains(key) && categoryRawMap.containsKey(key)) {
                int added = 0;
                for (String item : categoryRawMap.get(key)) {
                    if (item != null && !item.isBlank() && seenKeywords.add(item.toLowerCase().trim())) {
                        mixedKeywords.add(item.trim());
                        added++;
                        if (added >= 2) break;
                    }
                }
            }
        }

        for (String tw : topicWords) {
            if (mixedKeywords.size() >= 16) break;
            if (tw != null && !tw.isBlank() && seenKeywords.add(tw.toLowerCase().trim())) {
                mixedKeywords.add(tw.trim());
            }
        }

        if (mixedKeywords.size() < 12) {
            for (String key : activeCategoryKeys) {
                if (categoryRawMap.containsKey(key)) {
                    for (String item : categoryRawMap.get(key)) {
                        if (item != null && !item.isBlank() && seenKeywords.add(item.toLowerCase().trim())) {
                            mixedKeywords.add(item.trim());
                            if (mixedKeywords.size() >= 15) break;
                        }
                    }
                }
                if (mixedKeywords.size() >= 15) break;
            }
        }

        // 2. Collect mixed hashtags from topic, selected categories, and channel (no headings, placed at end)
        Set<String> seenHashtags = new HashSet<>();
        List<String> mixedHashtags = new ArrayList<>();

        for (String tw : topicWordsForHash) {
            String h = toHashtag(tw);
            if (!h.isEmpty() && seenHashtags.add(h.toLowerCase())) {
                mixedHashtags.add(h);
                if (mixedHashtags.size() >= 3) break;
            }
        }

        for (String catKey : activeCategoryKeys) {
            if (categoryRawMap.containsKey(catKey)) {
                for (String item : categoryRawMap.get(catKey)) {
                    String h = toHashtag(item);
                    if (!h.isEmpty() && seenHashtags.add(h.toLowerCase())) {
                        mixedHashtags.add(h);
                        break;
                    }
                }
            }
            if (mixedHashtags.size() >= 10) break;
        }

        if (channel != null && !channel.isBlank()) {
            String chHash = toHashtag(channel);
            if (!chHash.isEmpty() && seenHashtags.add(chHash.toLowerCase())) {
                mixedHashtags.add(chHash);
            }
        }

        if (mixedHashtags.size() < 6) {
            for (String tw : topicWordsForHash) {
                String h = toHashtag(tw);
                if (!h.isEmpty() && seenHashtags.add(h.toLowerCase())) {
                    mixedHashtags.add(h);
                    if (mixedHashtags.size() >= 6) break;
                }
            }
        }

        String descPara1 = "Learn how " + topicTitle + " works with practical, real-world walkthroughs from " + channel + " to improve your development workflow and technical skills.";
        String descPara2 = "In this walkthrough, you'll explore " + String.join(", ", mixedKeywords) + ", helping you master essential techniques and practical concepts.";
        String descPara3 = "Perfect for " + topicTitle + " beginners, developers, DevOps engineers, DBAs, QA engineers, cloud professionals, and interview preparation.";

        String brandLine = channel + " — Learn Smarter. Code Faster.";
        String hashtagLine = String.join(" ", mixedHashtags);
        String engagementQuestion = "What " + topicSubject + " strategy or technique do you use most — or what's your biggest challenge with it? Share below! \uD83D\uDC47";
        String closingCta = "If this helped you understand " + topicTitle + ", like \uD83D\uDC4D and subscribe to " + channel + " for more practical tutorials and technical concepts.";

        String descriptionTemplate = descPara1 + "\n\n" +
                descPara2 + "\n\n" +
                descPara3 + "\n\n" +
                brandLine + "\n\n" +
                hashtagLine + "\n\n" +
                engagementQuestion + "\n\n" +
                closingCta;

        Map<String, String> categoryTagsFields = new LinkedHashMap<>();
        for (TagCategory categoryEnum : TagCategory.values()) {
            if (activeCategoryKeys.contains(categoryEnum.getKey())) {
                List<String> rawItems = categoryRawMap.get(categoryEnum.getKey());
                StringBuilder sb = new StringBuilder();
                for (String item : rawItems) {
                    String candidate = sb.length() == 0 ? item : sb + ", " + item;
                    if (candidate.length() > 480) break;
                    sb.append(sb.length() == 0 ? item : ", " + item);
                }
                categoryTagsFields.put(categoryEnum.getKey(), sb.toString());
            }
        }

        String ctaText = "👍 Like the video if this was helpful.\n" +
                "🔔 Subscribe to " + channel + " for more practical " + topicTitle + " tutorials and Shorts.\n" +
                "💬 Drop your questions and doubts in the comments below!";

        return new MetadataDTO(titleOptions, descriptionTemplate, ctaText, categoryTagsFields);
    }

    private PlaylistMetadataDTO buildPlaylistMetadata(String channel, String topic, String topicSubject,
                                                       Map<String, List<String>> categoryRawMap,
                                                       String prompt) {
        String subject = topic;
        String subjectShort = extractShortSubject(subject, 4);

        List<String> plTopicWords = TOPIC_TEMPLATES.stream().map(t -> fillTool(t, subject.toLowerCase())).toList();
        List<String> plTopicWordsForHash = TOPIC_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList();
        List<String> plHighlights = plTopicWords.subList(0, Math.min(6, plTopicWords.size()));
        List<String> plScenarios = LOWCOMP_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).limit(4).toList();

        PlaylistMetadataDTO dto = new PlaylistMetadataDTO();

        dto.setPlaylistNames(List.of(
                subject + " — Full Playlist",
                subject + " Tutorials",
                "Complete " + subject + " Course",
                subject + ": Zero to Hero",
                "Learn " + subject + " Step by Step",
                subject + " Explained (Full Series)",
                subject + " Crash Course",
                subject + " for Beginners",
                "Master " + subject,
                subject + " Deep Dive Series"
        ));

        List<String> audiences = List.of(
                "Database Administrators (DBAs)", "Software Developers", "Backend Developers",
                "Frontend Developers", "QA Engineers", "Test Automation Engineers", "Data Engineers",
                "Data Analysts", "Cloud Engineers", "DevOps Engineers", "IT Professionals",
                "Students preparing for interviews", "Professionals transitioning into this field"
        );

        String intro = (prompt != null && !prompt.trim().isEmpty())
                ? "Master " + subject + " (" + prompt.trim() + ") with practical, real-world tutorials designed for developers, database professionals, QA engineers, and technical teams.\n\n"
                : "Master " + subject + " from beginner to advanced with practical, real-world tutorials designed for developers, database professionals, QA engineers, and technical teams.\n\n";

        String plDesc = intro +
                "This playlist covers " + String.join(", ", plHighlights) + ", and more — broken into short, focused, hands-on episodes.\n\n" +
                "You'll also explore real-world scenarios such as " + String.join(", ", plScenarios) + ", so what you learn here is immediately usable in real projects.\n\n" +
                "This playlist is perfect for:\n" +
                audiences.stream().map(a -> "• " + a).collect(Collectors.joining("\n")) + "\n\n" +
                "Whether you're just starting out or building production-grade skills, this playlist gives you practical, hands-on learning you can apply right away.";
        dto.setPlaylistDescription(plDesc);

        dto.setCtaText("👍 Like the videos if they help you.\n" +
                "🚀 Subscribe to " + channel + " for more " + subjectShort + " tutorials and practical content.\n" +
                "📚 Explore our other playlists to keep learning.");

        List<String> highReachPool = new ArrayList<>(plTopicWordsForHash.subList(0, Math.min(6, plTopicWordsForHash.size())));
        highReachPool.addAll(CHANNEL_TEMPLATES.subList(5, Math.min(20, CHANNEL_TEMPLATES.size())));
        dto.setHighReachHashtags(highReachPool.stream().distinct().limit(15).map(this::toHashtag).collect(Collectors.toList()));

        List<String> seoKeywordList = padTo30(categoryRawMap.get("seo"));
        dto.setSeoKeywordsList(seoKeywordList);

        StringBuilder tagsFieldBuilder = new StringBuilder();
        Set<String> seen = new HashSet<>();
        List<String> candidatePool = new ArrayList<>();
        candidatePool.add(subject.toLowerCase());
        candidatePool.add(channel.toLowerCase());
        candidatePool.addAll(plTopicWords);
        candidatePool.addAll(categoryRawMap.get("core").subList(0, 5));

        for (String kw : candidatePool) {
            if (seen.contains(kw)) continue;
            String candidateStr = tagsFieldBuilder.length() == 0 ? kw : tagsFieldBuilder + ", " + kw;
            if (candidateStr.length() > 480) break;
            if (tagsFieldBuilder.length() > 0) tagsFieldBuilder.append(", ");
            tagsFieldBuilder.append(kw);
            seen.add(kw);
        }
        dto.setPracticalTagsField(tagsFieldBuilder.toString());

        dto.setThumbnailConcept(
                "Visual Layout\n" +
                        "Left: A person looking stressed at a failing process/error\n" +
                        "Center: Large \"" + subjectShort + "\" graphic with a glowing highlight\n" +
                        "Right: A successful outcome dashboard with green checkmarks\n" +
                        "Background: Code editor, workflow icons, cloud/server graphics\n" +
                        "Dynamic accent lines suggesting motion and progress\n\n" +
                        "Thumbnail Text (3–5 words)\n" +
                        "Master " + subjectShort + "\n\n" +
                        "Colors\n" +
                        "Bright accent color, deep dark background, high-contrast white text, red for alerts, blue for tech elements\n\n" +
                        "Emotion Trigger\n" +
                        "From struggle to success — a clear before/after story."
        );

        dto.setThumbnailAiPrompt(
                "A professional YouTube thumbnail featuring a focused person working at multiple monitors displaying dashboards, code, and workflow diagrams related to " + subject + ". One side shows a failure state with red warning symbols and error indicators, while the other side transitions into a successful, automated outcome with green checkmarks and glowing progress indicators. Modern workspace with floating tech icons, code snippets, and workflow graphics. Cinematic YouTube thumbnail style with dramatic lighting, high contrast, dark background, sharp focus, vibrant accent colors for maximum CTR, ultra-detailed, modern technology environment, 16:9 aspect ratio."
        );

        List<String> lowCompRaw = LOWCOMP_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList();
        dto.setLowCompHashtags(lowCompRaw.stream().limit(15).map(this::toHashtag).collect(Collectors.toList()));
        dto.setLowCompKeywords(lowCompRaw.subList(0, Math.min(15, lowCompRaw.size())));

        List<String> hiddenRaw = HIDDEN_TEMPLATES.stream().map(t -> fillTool(t, topicSubject)).toList();
        dto.setHiddenHashtags(hiddenRaw.stream().limit(15).map(this::toHashtag).collect(Collectors.toList()));
        dto.setHiddenKeywords(hiddenRaw.subList(0, Math.min(15, hiddenRaw.size())));

        return dto;
    }

    private String fillTool(String template, String tool) {
        return template.replace("{tool}", tool);
    }

    private List<String> padTo30(List<String> list) {
        List<String> out = new ArrayList<>(list);
        if (out.size() >= 30) {
            return out.subList(0, 30);
        }
        String[] suffixes = {"tips", "explained", "guide", "notes"};
        int i = 0;
        while (out.size() < 30) {
            String base = list.get(i % list.size());
            out.add(base + " " + suffixes[i % suffixes.length]);
            i++;
        }
        return out;
    }

    private String toHashtag(String phrase) {
        String cleaned = phrase.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
        String[] words = cleaned.split("\\s+");
        StringBuilder sb = new StringBuilder("#");
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
            }
        }
        return sb.toString();
    }

    private String formatSeoTitle(String candidate) {
        if (candidate == null) return "";
        String trimmed = candidate.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= 100) {
            return trimmed;
        }
        boolean hasShorts = trimmed.toLowerCase().contains("#shorts");
        String suffix = hasShorts ? " #Shorts" : "";
        int maxBaseLen = 100 - suffix.length();
        String base = trimmed.replaceAll("(?i)\\s*#shorts", "").trim();
        if (base.length() > maxBaseLen) {
            base = base.substring(0, maxBaseLen).replaceAll("\\s+\\S*$", "").trim();
        }
        String result = (base + suffix).trim();
        if (result.length() > 100) {
            result = result.substring(0, 100);
        }
        return result;
    }

    private String extractShortSubject(String text, int maxWords) {
        String cleaned = text.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        String[] words = cleaned.split(" ");
        if (words.length == 0 || words[0].isEmpty()) return "";
        return String.join(" ", Arrays.copyOfRange(words, 0, Math.min(maxWords, words.length)));
    }

    public String extractTopicFromPrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return null;
        }
        String p = prompt.trim();
        List<java.util.regex.Pattern> patterns = List.of(
                java.util.regex.Pattern.compile("(?:^|\\n)\\s*(?:the\\s+|my\\s+)?topic\\s*[:=]\\s*[\"']?([^\"'\\n\\r]+)[\"']?", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("(?:my\\s+|the\\s+)?topic\\s+(?:is|:|=)\\s+([^.\\n\\r,;]+)", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("(?:create|generate|provide|write|make)\\s+(?:the\\s+)?(?:youtube\\s+)?(?:metadata|tags|keywords|titles|hashtags|description|content|video)\\s+(?:for|about|on)\\s+(?:(?:the\\s+)?topic\\s*[:=]?\\s*)?([^.\\n\\r,;]+)", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("(?:video|metadata|tags|tutorial|guide)\\s+(?:for|about|on)\\s+(?:(?:the\\s+)?topic\\s*[:=]?\\s*)?([^.\\n\\r,;]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
        );

        for (java.util.regex.Pattern pattern : patterns) {
            java.util.regex.Matcher m = pattern.matcher(p);
            if (m.find() && m.group(1) != null) {
                String candidate = cleanTopicString(m.group(1));
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }

        // If the prompt is a single line / direct topic input (e.g. "Azure DevOps Interview Questions" or "The Topic: Azure DevOps Interview Questions")
        if (p.length() < 100 && !p.contains("\n")) {
            String candidate = cleanTopicString(p);
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }

        return null;
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
}
