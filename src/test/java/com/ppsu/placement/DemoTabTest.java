package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** The Architecture Demo tab (/demo): the page, and the two static files it embeds. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:demotab;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class DemoTabTest {

    @Autowired MockMvc mvc;

    @Test
    void demo_page_renders_in_the_layout_and_embeds_both_pages() throws Exception {
        mvc.perform(get("/demo")).andExpect(status().isOk()).andExpect(view().name("demo"))
                .andExpect(content().string(containsString("Architecture Demo")))
                .andExpect(content().string(containsString("walkthrough.html")))
                .andExpect(content().string(containsString("flow-explorer.html")));
    }

    @Test
    void demo_page_links_all_15_artifacts_including_the_three_walkthrough_parts() throws Exception {
        String html = mvc.perform(get("/demo")).andReturn().getResponse().getContentAsString();
        assertThat(html.split("href=\"https://claude.ai/", -1).length - 1).isEqualTo(15);
        assertThat(html).contains("claude.ai/artifact/TA4zJFNPtaRhSUwnzdDv27", "claude.ai/artifact/Gh32D4cuCxvw9DJoRdbuFU",
                "claude.ai/artifact/MCtoGsEd4FTa4FWSN7Uhha");
    }

    @Test
    void the_walkthrough_is_on_the_classpath_and_covers_the_stack() throws Exception {
        String html = read(new ClassPathResource("static/demo/walkthrough.html"));
        assertThat(html).contains("Placement Portal").contains("N+1");
        mvc.perform(get("/demo/walkthrough.html")).andExpect(status().isOk());
    }

    @Test
    void the_flow_explorer_copy_matches_docs() throws Exception {
        String served = read(new ClassPathResource("static/demo/flow-explorer.html"));
        assertThat(served).contains("Flow Explorer");
        assertThat(served).as("static/demo/flow-explorer.html must equal docs/flow-explorer.html")
                .isEqualTo(Files.readString(Path.of("docs", "flow-explorer.html"), StandardCharsets.UTF_8));
        mvc.perform(get("/demo/flow-explorer.html")).andExpect(status().isOk());
    }

    private static String read(ClassPathResource r) throws Exception {
        return new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
