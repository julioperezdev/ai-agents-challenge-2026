package com.aichallenge.agents.gitdiffrfc.infrastructure.output;

import com.aichallenge.agents.gitdiffrfc.domain.RfcDocument;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRfcRenderer {

    public String render(RfcDocument document) {
        String markdown = sanitize(document.markdown());
        if (markdown.isBlank()) {
            throw new IllegalStateException("RFC writer returned an empty document.");
        }
        return markdown.endsWith("\n") ? markdown : markdown + "\n";
    }

    private String sanitize(String markdown) {
        if (markdown == null) {
            return "";
        }
        String trimmed = markdown.trim();
        trimmed = removeReasoningBlocks(trimmed);
        trimmed = normalizeSpacing(trimmed);
        if (trimmed.startsWith("```markdown")) {
            trimmed = trimmed.substring("```markdown".length()).trim();
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring("```".length()).trim();
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    private String normalizeSpacing(String markdown) {
        return markdown
                .replace('\u202F', ' ')
                .replace('\u00A0', ' ');
    }

    private String removeReasoningBlocks(String markdown) {
        String cleaned = markdown.replaceAll("(?is)<reasoning>.*?</reasoning>", "").trim();
        cleaned = cleaned.replaceAll("(?is)<think>.*?</think>", "").trim();
        return cleaned;
    }
}
