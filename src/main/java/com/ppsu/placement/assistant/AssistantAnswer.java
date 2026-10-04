package com.ppsu.placement.assistant;

import java.util.List;
import java.util.Map;

/**
 * What the browser receives. 'tool' and 'args' are shown in the "what the AI asked for" panel;
 * 'steps' is the pipeline trace (AI proposes, software validates, software acts) for the session demo.
 */
public record AssistantAnswer(String question, String mode, String tool, Map<String, Object> args,
                              String summary, List<String> columns, List<List<String>> rows, String error,
                              List<String> steps) {

    static AssistantAnswer ok(String question, String mode, ToolRequest req, ToolResult r, List<String> steps) {
        return new AssistantAnswer(question, mode, req.tool(), req.args(), r.summary(), r.columns(), r.rows(), null, steps);
    }

    static AssistantAnswer error(String question, String mode, String message) {
        return error(question, mode, message, List.of());
    }

    static AssistantAnswer error(String question, String mode, String message, List<String> steps) {
        return new AssistantAnswer(question, mode, null, Map.of(), null, List.of(), List.of(), message, steps);
    }
}
