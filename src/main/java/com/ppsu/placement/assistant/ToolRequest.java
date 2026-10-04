package com.ppsu.placement.assistant;

import java.util.Map;

/** What the AI asked for: a tool name plus arguments. Untrusted until validated. */
public record ToolRequest(String tool, Map<String, Object> args) {
    public ToolRequest {
        // Map.copyOf rejects null values, and a model can send {"status": null}; drop them first
        args = args == null ? Map.of() : args.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
