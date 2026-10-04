package com.ppsu.placement.assistant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.ppsu.placement.application.ApplicationStatus;

/**
 * AI proposes (a tool request). Software validates (whitelist, enum, known company).
 * Software acts (fixed read-only queries). Numbers in the answer come from the database.
 */
@Service
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);
    private static final int MAX_QUESTION_LENGTH = 300;

    private final AiClient ai;
    private final AssistantQueries queries;

    public AssistantService(AiClient ai, AssistantQueries queries) {
        this.ai = ai;
        this.queries = queries;
    }

    public String mode() { return ai.mode(); }

    public AssistantAnswer ask(String rawQuestion) {
        String question = rawQuestion == null ? "" : rawQuestion.trim();
        if (question.isEmpty()) {
            return AssistantAnswer.error(question, ai.mode(), "Please type a question.");
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            return AssistantAnswer.error("", ai.mode(), "Please keep the question under " + MAX_QUESTION_LENGTH + " characters.");
        }

        List<String> steps = new ArrayList<>();
        ToolRequest request;
        try {
            request = ai.chooseTool(question, queries.companyNames());
            steps.add("1. AI proposes (" + ai.mode() + "): tool " + request.tool() + " " + request.args());
        } catch (AiUnavailableException e) {
            log.warn("assistant: AI unavailable ({})", e.getMessage());
            return AssistantAnswer.error(question, ai.mode(), e.getMessage());
        }

        // Audit line: which tool, never the student data
        log.info("assistant mode={} tool={} args={}", ai.mode(), request.tool(), request.args());

        if (!ToolCatalog.NAMES.contains(request.tool())) {
            return AssistantAnswer.error(question, ai.mode(), "The AI asked for a tool that does not exist, so nothing was run.",
                    List.of(steps.get(0), "2. Software validates: tool is not in the whitelist, rejected"));
        }
        try {
            steps.add("2. Software validates: tool is whitelisted, arguments checked against enum and known companies");
            ToolResult result = run(request);
            steps.add("3. Software acts: fixed read-only query returned " + result.rows().size() + " row(s) from the database");
            return AssistantAnswer.ok(question, ai.mode(), request, result, steps);
        } catch (IllegalArgumentException e) {
            return AssistantAnswer.error(question, ai.mode(), "The AI's request was rejected: " + e.getMessage(),
                    List.of(steps.get(0), "2. Software validates: arguments rejected, no query run"));
        }
    }

    private ToolResult run(ToolRequest req) {
        return switch (req.tool()) {
            case ToolCatalog.PLACED_STUDENTS -> applicationTable(queries.applicationsByStatus(ApplicationStatus.SELECTED),
                    "placement(s)", true);
            case ToolCatalog.APPLICATIONS_BY_STATUS -> {
                ApplicationStatus status = requireStatus(req.args().get("status"));
                yield applicationTable(queries.applicationsByStatus(status), "application(s) with status " + status, false);
            }
            case ToolCatalog.STUDENTS_FOR_COMPANY -> forCompany(req);
            case ToolCatalog.STUDENTS_WITHOUT_APPLICATIONS -> {
                List<Object[]> rows = queries.studentsWithoutApplications();
                yield new ToolResult(rows.isEmpty() ? "Every student has applied to at least one job."
                        : rows.size() + " student(s) have not applied anywhere yet.",
                        List.of("Student", "Email", "Branch", "CGPA"), toStrings(rows));
            }
            case ToolCatalog.APPLICATIONS_PER_COMPANY -> new ToolResult("Applications per company, highest first.",
                    List.of("Company", "Applications"), toStrings(queries.applicationsPerCompany()));
            case ToolCatalog.PLACEMENT_SUMMARY -> summary();
            case ToolCatalog.CANNOT_ANSWER -> ToolResult.message(
                    "I can only answer placement questions: who is placed and where, applications by status, "
                            + "students for a company, applications per company, and students without applications.");
            default -> throw new IllegalArgumentException("unknown tool");
        };
    }

    private ToolResult forCompany(ToolRequest req) {
        String asked = req.args().get("company") instanceof String s ? s.trim() : "";
        if (asked.isEmpty() || asked.length() > 80) throw new IllegalArgumentException("company name is missing or too long");

        String known = queries.companyNames().stream().filter(n -> n.equalsIgnoreCase(asked)).findFirst().orElse(null);
        if (known == null) {
            return ToolResult.message("I could not find a company with that name. Known companies: "
                    + String.join(", ", queries.companyNames()) + ".");
        }
        Object rawStatus = req.args().get("status");
        ApplicationStatus status = rawStatus == null ? null : requireStatus(rawStatus);
        return applicationTable(queries.applicationsForCompany(known, status),
                "application(s) at " + known + (status != null ? " with status " + status : ""), false);
    }

    private ToolResult summary() {
        long students = queries.studentCount();
        long placed = queries.placedStudentCount();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        ToolCatalog.STATUSES.forEach(s -> byStatus.put(s, 0L));
        for (Object[] row : queries.applicationCountsByStatus()) byStatus.put(String.valueOf(row[0]), (Long) row[1]);

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Students in the portal", String.valueOf(students)));
        rows.add(List.of("Students placed", String.valueOf(placed)));
        rows.add(List.of("Placement percentage", students == 0 ? "0%" : String.format(Locale.ROOT, "%.1f%%", placed * 100.0 / students)));
        byStatus.forEach((s, n) -> rows.add(List.of("Applications: " + s, String.valueOf(n))));
        return new ToolResult("Placement summary.", List.of("Measure", "Value"), rows);
    }

    private ToolResult applicationTable(List<Object[]> rows, String what, boolean countStudents) {
        List<List<String>> table = toStrings(rows);
        if (table.isEmpty()) return ToolResult.message("No matching records.");
        String text = table.size() + " " + what;
        if (countStudents) {
            Set<String> students = new HashSet<>();
            table.forEach(r -> students.add(r.get(0)));
            text += " for " + students.size() + " student(s)";
        }
        return new ToolResult(text + ".", List.of("Student", "Company", "Role", "Status"), table);
    }

    private static ApplicationStatus requireStatus(Object value) {
        if (value instanceof String s && ToolCatalog.STATUSES.contains(s.trim().toUpperCase(Locale.ROOT))) {
            return ApplicationStatus.valueOf(s.trim().toUpperCase(Locale.ROOT));
        }
        throw new IllegalArgumentException("status must be one of " + ToolCatalog.STATUSES);
    }

    private static List<List<String>> toStrings(List<Object[]> rows) {
        List<List<String>> out = new ArrayList<>();
        for (Object[] row : rows) {
            List<String> cells = new ArrayList<>();
            for (Object cell : row) cells.add(String.valueOf(cell));
            out.add(cells);
        }
        return out;
    }
}
