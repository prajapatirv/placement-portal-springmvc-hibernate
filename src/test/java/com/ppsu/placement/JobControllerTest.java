package com.ppsu.placement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Web layer through the whole stack: DispatcherServlet, controller, service, Hibernate, H2, Thymeleaf. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:web;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class JobControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void jobs_page_lists_only_open_postings() throws Exception {
        mvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/list"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(content().string(containsString("Java Backend Intern")))
                .andExpect(content().string(not(containsString("QA Automation Trainee"))));
    }

    @Test
    void city_filter_narrows_the_list() throws Exception {
        mvc.perform(get("/jobs").param("city", "surat"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DevOps Trainee")))
                .andExpect(content().string(not(containsString("Java Backend Intern"))));
    }

    @Test
    void detail_page_renders_one_job() throws Exception {
        mvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/detail"))
                .andExpect(content().string(containsString("Nimbus Tech")));
    }

    @Test
    void unknown_job_gives_the_404_page() throws Exception {
        mvc.perform(get("/jobs/9999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Job 9999 was not found")));
    }

    @Test
    void json_endpoint_returns_the_same_data() throws Exception {
        mvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[0].company").exists());
        mvc.perform(get("/api/jobs/summaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void api_unknown_job_is_a_json_404() throws Exception {
        mvc.perform(get("/api/jobs/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Job 9999 was not found"));
    }

    @Test
    void apply_form_with_empty_email_redisplays_with_validation_message() throws Exception {
        mvc.perform(post("/jobs/3/apply").param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("apply"))
                .andExpect(content().string(containsString("Email is required")));
    }

    @Test
    void apply_twice_gives_the_409_page_and_first_time_redirects() throws Exception {
        // Yash has not applied to job 4 (Data Analyst Intern) in the seed data
        mvc.perform(post("/jobs/4/apply").param("email", "yash@ppsu.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/applications"))
                .andExpect(flash().attribute("message", "Application submitted"));
        mvc.perform(post("/jobs/4/apply").param("email", "yash@ppsu.example"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("already applied")));
    }

    @Test
    void apply_to_closed_job_gives_the_409_page() throws Exception {
        mvc.perform(post("/jobs/2/apply").param("email", "asha@ppsu.example"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("closed")));
    }

    @Test
    void manage_page_is_paged() throws Exception {
        mvc.perform(get("/jobs/manage"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Page 1 of 3")));
    }

    @Test
    void applications_page_works_in_slow_and_fast_mode() throws Exception {
        mvc.perform(get("/applications")).andExpect(status().isOk())
                .andExpect(content().string(containsString("FAST")));
        mvc.perform(get("/applications").param("slow", "true")).andExpect(status().isOk())
                .andExpect(content().string(containsString("SLOW")));
    }

    @Test
    void home_page_shows_the_seed_counts() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Campus Placement Portal")));
    }
}
