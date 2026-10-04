package com.ppsu.placement.assistant;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Keyword rules an admin adds while the app runs (MOCK mode). In memory only: gone after a restart.
 * A rule can only point at a whitelisted tool, so a custom keyword never widens what the assistant can do.
 */
@Component
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class KeywordRules {

    public record Rule(String keyword, String tool, String status) {}

    static final int MAX_RULES = 50;
    static final int MAX_KEYWORD_LENGTH = 60;

    private final List<Rule> rules = new CopyOnWriteArrayList<>();

    public List<Rule> all() { return List.copyOf(rules); }

    /** Validates, then adds (or replaces the rule with the same keyword). Throws IllegalArgumentException. */
    public synchronized Rule add(String keyword, String tool, String status) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (k.length() < 3 || k.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("keyword must be 3 to " + MAX_KEYWORD_LENGTH + " characters");
        }
        if (tool == null || !ToolCatalog.NAMES.contains(tool) || ToolCatalog.CANNOT_ANSWER.equals(tool)
                || ToolCatalog.STUDENTS_FOR_COMPANY.equals(tool)) {
            throw new IllegalArgumentException("tool must be one of the whitelisted tools that need no company name");
        }
        String s = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        if (ToolCatalog.APPLICATIONS_BY_STATUS.equals(tool) && !ToolCatalog.STATUSES.contains(s)) {
            throw new IllegalArgumentException("status must be one of " + ToolCatalog.STATUSES);
        }
        if (!ToolCatalog.APPLICATIONS_BY_STATUS.equals(tool)) s = null;

        rules.removeIf(r -> r.keyword().equals(k));
        if (rules.size() >= MAX_RULES) throw new IllegalArgumentException("too many rules (max " + MAX_RULES + ")");
        Rule rule = new Rule(k, tool, s);
        rules.add(rule);
        return rule;
    }

    public boolean remove(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return rules.removeIf(r -> r.keyword().equals(k));
    }

    /** First rule whose keyword appears in the (already lower-cased) question, or null. */
    ToolRequest match(String lowerQuestion) {
        for (Rule r : rules) {
            if (lowerQuestion.contains(r.keyword())) {
                return new ToolRequest(r.tool(), r.status() == null ? Map.of() : Map.of("status", r.status()));
            }
        }
        return null;
    }
}
