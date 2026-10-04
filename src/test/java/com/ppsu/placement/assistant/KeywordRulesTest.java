package com.ppsu.placement.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class KeywordRulesTest {

    private final KeywordRules rules = new KeywordRules();
    private final MockAiClient ai = new MockAiClient(rules);

    @Test
    void customKeywordRoutesToWhitelistedTool() {
        rules.add("Top Recruiters", ToolCatalog.APPLICATIONS_PER_COMPANY, null);
        assertThat(ai.chooseTool("Show me our TOP recruiters", List.of()).tool())
                .isEqualTo(ToolCatalog.APPLICATIONS_PER_COMPANY);
    }

    @Test
    void statusRuleCarriesItsStatus() {
        rules.add("good news", ToolCatalog.APPLICATIONS_BY_STATUS, "selected");
        assertThat(ai.chooseTool("any good news?", List.of()).args()).isEqualTo(Map.of("status", "SELECTED"));
    }

    @Test
    void invalidRulesAreRejected() {
        assertThatThrownBy(() -> rules.add("x", ToolCatalog.PLACED_STUDENTS, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rules.add("delete all", "delete_students", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rules.add("by status", ToolCatalog.APPLICATIONS_BY_STATUS, "BOGUS")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removedRuleStopsMatching() {
        rules.add("recruiters", ToolCatalog.APPLICATIONS_PER_COMPANY, null);
        rules.remove("recruiters");
        assertThat(ai.chooseTool("recruiters", List.of()).tool()).isEqualTo(ToolCatalog.CANNOT_ANSWER);
    }
}
