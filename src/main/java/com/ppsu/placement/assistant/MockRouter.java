package com.ppsu.placement.assistant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Keyword rules that pretend to be the model. Plain Java, no Spring, so it is easy to unit test.
 * Good enough for a demo; this is exactly the part a real model does better.
 */
public final class MockRouter {
    private MockRouter() {}

    public static ToolRequest route(String question, List<String> companies) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);

        if (has(q, "without application", "not applied", "no application", "haven't applied", "havent applied",
                "never applied", "yet to apply", "not yet applied", "zero application")) {
            return new ToolRequest(ToolCatalog.STUDENTS_WITHOUT_APPLICATIONS, Map.of());
        }
        if (has(q, "per company", "each company", "by company", "company wise", "companywise", "company-wise")) {
            return new ToolRequest(ToolCatalog.APPLICATIONS_PER_COMPANY, Map.of());
        }

        String status = inferStatus(q);
        String company = findCompany(q, companies);
        if (company != null) {
            Map<String, Object> args = new LinkedHashMap<>();
            args.put("company", company);
            if (status != null) args.put("status", status);
            return new ToolRequest(ToolCatalog.STUDENTS_FOR_COMPANY, args);
        }
        if ("SELECTED".equals(status)) {
            return new ToolRequest(ToolCatalog.PLACED_STUDENTS, Map.of());
        }
        if (status != null) {
            return new ToolRequest(ToolCatalog.APPLICATIONS_BY_STATUS, Map.of("status", status));
        }
        if (has(q, "how many", "percentage", "percent", "placement rate", "summary", "overall", "statistics", "stats",
                "placement")) {
            return new ToolRequest(ToolCatalog.PLACEMENT_SUMMARY, Map.of());
        }
        return new ToolRequest(ToolCatalog.CANNOT_ANSWER, Map.of("reason", "No rule matched this question."));
    }

    private static String inferStatus(String q) {
        if (has(q, "shortlist")) return "SHORTLISTED";
        if (has(q, "reject")) return "REJECTED";
        if (has(q, "placed", "selected", "hired", "got a job", "got job", "offer")) return "SELECTED";
        if (has(q, "applied", "pending")) return "APPLIED";
        return null;
    }

    /** Longest company name found inside the question wins. */
    private static String findCompany(String q, List<String> companies) {
        String best = null;
        for (String c : companies) {
            if (q.contains(c.toLowerCase(Locale.ROOT)) && (best == null || c.length() > best.length())) best = c;
        }
        return best;
    }

    private static boolean has(String q, String... needles) {
        for (String n : needles) if (q.contains(n)) return true;
        return false;
    }
}
