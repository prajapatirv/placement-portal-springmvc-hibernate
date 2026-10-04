package com.ppsu.placement.assistant;

import java.util.List;

/** A plain table the page can draw. Numbers in the summary come from the database, never from the model. */
public record ToolResult(String summary, List<String> columns, List<List<String>> rows) {
    public static ToolResult message(String summary) {
        return new ToolResult(summary, List.of(), List.of());
    }
}
