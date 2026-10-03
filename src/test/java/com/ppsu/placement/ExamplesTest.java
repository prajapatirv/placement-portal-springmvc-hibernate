package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ppsu.placement.application.ApplicationService;
import com.ppsu.placement.common.RequestLog;
import com.ppsu.placement.examples.Example;
import com.ppsu.placement.examples.ExampleCatalog;
import com.ppsu.placement.job.JobForm;
import com.ppsu.placement.job.JobService;
import com.ppsu.placement.job.JobStatus;
import com.ppsu.placement.student.Student;
import com.ppsu.placement.student.StudentRepository;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** The session examples (docs/10, page /examples): every Run button works and every number we say aloud is true. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:examples;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ExamplesTest {

    @Autowired MockMvc mvc;
    @Autowired ExampleCatalog catalog;
    @Autowired StudentRepository students;
    @Autowired ApplicationService service;
    @Autowired JobService jobService;
    @Autowired EntityManagerFactory emf;
    @Autowired RequestLog requestLog;

    Statistics stats;

    @BeforeEach
    void resetStatistics() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
    }

    // ---- the guide itself ----

    @Test
    void index_and_every_example_page_render() throws Exception {
        mvc.perform(get("/examples")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Map a class to a table")));
        for (Example e : catalog.all()) {
            mvc.perform(get("/examples/" + e.id)).andExpect(status().isOk())
                    .andExpect(content().string(containsString(e.title)));
        }
        mvc.perform(get("/examples/zzz")).andExpect(status().isNotFound());
    }

    @Test
    void every_run_button_that_does_not_change_data_gives_the_status_we_promise() throws Exception {
        for (Example e : catalog.all()) {
            for (Example.Run r : e.runs) {
                if (r.mutates()) continue;
                var req = request(HttpMethod.valueOf(r.method()), r.url());
                if (r.body() != null) req.contentType(r.contentType()).content(r.body());
                mvc.perform(req).andExpect(status().is(r.status()));
            }
        }
    }

    @Test
    void every_example_explains_how_it_works_inside() {
        for (Example e : catalog.all()) {
            assertThat(e.inside).as("inside section of " + e.id).isNotEmpty();
        }
    }

    // ---- H2, H4: queries ----

    @Test
    void h2_derived_query_gives_ce_students_with_cgpa_8_or_more_best_first() {
        var found = students.findByBranchIgnoreCaseAndCgpaGreaterThanEqualOrderByCgpaDesc("ce", new BigDecimal("8.0"));
        assertThat(found).extracting(Student::getName).containsExactly("Meera Desai", "Asha Patel", "Nisha Parmar");
    }

    @Test
    void h4_applicants_per_job_keeps_jobs_with_zero_applicants() throws Exception {
        // seed data: every one of the 12 postings has at least one applicant
        mvc.perform(get("/api/jobs/popular")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[11].applicants").value(1));

        // the demo step: add a posting nobody applied to. The left join keeps it, with count 0.
        Long id = jobService.create(new JobForm(1L, "Zero Applicant Demo", new BigDecimal("1.0"),
                new BigDecimal("2.0"), java.time.LocalDate.now().plusMonths(1), JobStatus.OPEN));
        try {
            mvc.perform(get("/api/jobs/popular")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(13))
                    .andExpect(jsonPath("$[12].title").value("Zero Applicant Demo"))
                    .andExpect(jsonPath("$[12].applicants").value(0));
        } finally {
            jobService.delete(id);
        }
    }

    // ---- H3 and H5: the same page, two fetch plans ----

    @Test
    void h3_my_applications_naive_is_n_plus_one_and_h5_entity_graph_is_one_statement() {
        assertThat(service.mine("asha@ppsu.example", true)).hasSize(3);
        long naive = stats.getPrepareStatementCount();
        assertThat(naive).isEqualTo(8);                                // 1 list + 1 student + 3 jobs + 3 companies

        stats.clear();
        assertThat(service.mine("asha@ppsu.example", false)).hasSize(3);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    // ---- H6, H8: the lab ----

    @Test
    void h6_lifecycle_dirty_checking_and_first_level_cache() throws Exception {
        mvc.perform(get("/examples/run/h6/lifecycle")).andExpect(status().isOk())
                .andExpect(jsonPath("$.steps.length()").value(5))
                .andExpect(jsonPath("$.steps[0].state").value("TRANSIENT"))
                .andExpect(jsonPath("$.steps[3].evidence", containsString("same object as s? false")));
        mvc.perform(get("/examples/run/h6/dirty-checking")).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAfter").value("SHORTLISTED"))
                .andExpect(jsonPath("$.statementsAtFlush").value(1));
        mvc.perform(get("/examples/run/h6/first-level-cache")).andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFindsInOneTransaction.statements").value(1))
                .andExpect(jsonPath("$.twoFindsInOneTransaction.sameObject").value(true))
                .andExpect(jsonPath("$.afterEmClear.statements").value(1));
    }

    @Test
    void h8_bulk_update_is_one_statement_but_skips_the_version() throws Exception {
        mvc.perform(get("/examples/run/h8/compare")).andExpect(status().isOk())
                .andExpect(jsonPath("$.loopWithDirtyChecking.statements").value(6))
                .andExpect(jsonPath("$.loopWithDirtyChecking.versionIncremented").value(true))
                .andExpect(jsonPath("$.oneBulkUpdate.statements").value(1))
                .andExpect(jsonPath("$.oneBulkUpdate.versionIncremented").value(false))
                .andExpect(jsonPath("$.oneBulkUpdate.rowsUpdated").value(3));
        // both runs rolled back: nothing was shortlisted
        mvc.perform(get("/applications")).andExpect(status().isOk());
    }

    @Test
    void h1_mapping_agrees_with_the_schema() throws Exception {
        mvc.perform(get("/examples/run/h1/mapping")).andExpect(status().isOk())
                .andExpect(jsonPath("$.columnsMissingInDatabase.length()").value(0));
    }

    // ---- M1, M5: parameters and validation errors ----

    @Test
    void m1_numeric_parameter_is_converted_and_bad_input_is_400() throws Exception {
        mvc.perform(get("/api/jobs/paying").param("minLpa", "3.7")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Python Data Engineer Trainee"));
        mvc.perform(get("/api/jobs/paying")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/jobs/paying").param("minLpa", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void m5_empty_json_body_lists_the_failing_fields() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.jobId").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // ---- M4: 201, then 409, then clean up ----

    @Test
    void m4_apply_returns_201_with_location_then_409_and_can_be_undone() throws Exception {
        String body = "{\"jobId\":1,\"email\":\"isha@ppsu.example\"}";
        MvcResult created = mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.student").value("Isha Joshi"))
                .andReturn();
        String location = created.getResponse().getHeader("Location");

        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }

    // ---- M6: the interceptor ----

    @Test
    void m6_interceptor_records_the_sql_count_of_each_request() throws Exception {
        requestLog.clear();
        mvc.perform(get("/applications?slow=true")).andExpect(status().isOk());
        mvc.perform(get("/applications")).andExpect(status().isOk());

        var recent = requestLog.recent();                              // newest first
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).uri()).isEqualTo("/applications");
        assertThat(recent.get(0).sql()).isEqualTo(1);
        assertThat(recent.get(1).uri()).isEqualTo("/applications?slow=true");
        assertThat(recent.get(1).sql()).isEqualTo(26);
    }

    // ---- M2: the rule lives in Java ----

    @Test
    void m2_closing_soon_is_true_only_within_three_days() {
        var today = java.time.LocalDate.now();
        assertThat(view(today.plusDays(1)).closingSoon()).isTrue();
        assertThat(view(today.plusDays(3)).closingSoon()).isTrue();
        assertThat(view(today.plusDays(4)).closingSoon()).isFalse();
        assertThat(view(today.minusDays(1)).closingSoon()).isFalse();
    }

    private static com.ppsu.placement.job.JobView view(java.time.LocalDate last) {
        return new com.ppsu.placement.job.JobView(1L, "t", 1L, "c", "city", BigDecimal.ONE, BigDecimal.TEN, last,
                com.ppsu.placement.job.JobStatus.OPEN);
    }
}
