package com.ppsu.placement.assistant;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default mode. Works offline, needs no API key. Runtime keyword rules are tried before the built-in ones. */
@Component
@ConditionalOnProperty(name = "assistant.mode", havingValue = "mock", matchIfMissing = true)
public class MockAiClient implements AiClient {

    private final KeywordRules customRules;

    public MockAiClient(KeywordRules customRules) { this.customRules = customRules; }

    @Override
    public String mode() { return "MOCK"; }

    @Override
    public ToolRequest chooseTool(String question, List<String> companyNames) {
        ToolRequest custom = customRules.match(question.toLowerCase(Locale.ROOT));
        return custom != null ? custom : MockRouter.route(question, companyNames);
    }
}
