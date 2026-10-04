package com.ppsu.placement.assistant;

import java.util.List;

/**
 * The one seam between our code and "the AI". Two implementations: MockAiClient (no network, no key)
 * and ClaudeAiClient (real model). The rest of the feature cannot tell them apart.
 */
public interface AiClient {
    /** "MOCK" or "LIVE" - shown as a badge on the page. */
    String mode();

    /** Turn a free-text question into ONE tool request. The model never sees student data. */
    ToolRequest chooseTool(String question, List<String> companyNames) throws AiUnavailableException;
}
