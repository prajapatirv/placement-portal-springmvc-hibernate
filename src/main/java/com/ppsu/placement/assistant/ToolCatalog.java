package com.ppsu.placement.assistant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The whitelist: the only things the assistant can ever do. All read-only. */
public final class ToolCatalog {
    public static final String PLACED_STUDENTS = "placed_students";
    public static final String APPLICATIONS_BY_STATUS = "applications_by_status";
    public static final String STUDENTS_FOR_COMPANY = "students_for_company";
    public static final String STUDENTS_WITHOUT_APPLICATIONS = "students_without_applications";
    public static final String APPLICATIONS_PER_COMPANY = "applications_per_company";
    public static final String PLACEMENT_SUMMARY = "placement_summary";
    public static final String CANNOT_ANSWER = "cannot_answer";

    public static final List<String> STATUSES = List.of("APPLIED", "SHORTLISTED", "REJECTED", "SELECTED");

    public static final Set<String> NAMES = Set.of(PLACED_STUDENTS, APPLICATIONS_BY_STATUS, STUDENTS_FOR_COMPANY,
            STUDENTS_WITHOUT_APPLICATIONS, APPLICATIONS_PER_COMPANY, PLACEMENT_SUMMARY, CANNOT_ANSWER);

    private ToolCatalog() {}

    /** Tool definitions in the shape the Claude Messages API expects. */
    public static List<Map<String, Object>> claudeTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(tool(PLACED_STUDENTS,
                "List students who have been placed (selected), with the company and role.", Map.of(), List.of()));
        tools.add(tool(APPLICATIONS_BY_STATUS,
                "List applications that have one given status (APPLIED, SHORTLISTED, REJECTED or SELECTED).",
                Map.of("status", enumProp("Application status.")), List.of("status")));
        tools.add(tool(STUDENTS_FOR_COMPANY,
                "List the students who applied to one company, optionally only those with one status.",
                Map.of("company", stringProp("Company name exactly as in the known companies list."),
                        "status", enumProp("Optional status filter.")), List.of("company")));
        tools.add(tool(STUDENTS_WITHOUT_APPLICATIONS,
                "List students who have not applied to any job yet.", Map.of(), List.of()));
        tools.add(tool(APPLICATIONS_PER_COMPANY,
                "Count applications for each company, highest first.", Map.of(), List.of()));
        tools.add(tool(PLACEMENT_SUMMARY,
                "Overall numbers: total students, placed students, placement percentage, applications by status.",
                Map.of(), List.of()));
        tools.add(tool(CANNOT_ANSWER,
                "Use when the question is not about placements, or none of the other tools fits.",
                Map.of("reason", stringProp("Short reason.")), List.of()));
        return tools;
    }

    private static Map<String, Object> tool(String name, String description,
                                            Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("name", name);
        t.put("description", description);
        t.put("input_schema", schema);
        return t;
    }

    private static Map<String, Object> stringProp(String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> enumProp(String description) {
        return Map.of("type", "string", "enum", STATUSES, "description", description);
    }
}
