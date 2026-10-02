package com.ppsu.placement;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Create, read, update and delete for Company, Student and JobPosting, including the guard rails. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:crud;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CrudFlowTest {

    @Autowired MockMvc mvc;

    // ---------------- Company ----------------

    @Test
    void company_create_edit_delete() throws Exception {
        mvc.perform(post("/companies").param("name", "Zenith Labs").param("city", "Anand").param("website", ""))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/companies"));
        mvc.perform(get("/companies")).andExpect(content().string(containsString("Zenith Labs")));

        // ids 1-5 are seeded, the new company is 6
        mvc.perform(post("/companies/6").param("name", "Zenith Labs Pvt").param("city", "Anand").param("website", "https://z.example"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/companies")).andExpect(content().string(containsString("Zenith Labs Pvt")));

        mvc.perform(post("/companies/6/delete")).andExpect(flash().attribute("message", "Company deleted"));
        mvc.perform(get("/companies")).andExpect(content().string(org.hamcrest.Matchers.not(containsString("Zenith Labs Pvt"))));
    }

    @Test
    void company_validation_and_duplicate_name_show_form_errors() throws Exception {
        mvc.perform(post("/companies").param("name", "").param("city", "").param("website", ""))
                .andExpect(status().isOk()).andExpect(view().name("companies/form"))
                .andExpect(content().string(containsString("Name is required")))
                .andExpect(content().string(containsString("City is required")));
        mvc.perform(post("/companies").param("name", "nimbus tech").param("city", "X").param("website", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void company_with_postings_cannot_be_deleted() throws Exception {
        mvc.perform(post("/companies/1/delete"))
                .andExpect(flash().attribute("error", "Cannot delete Nimbus Tech: it still has 2 job posting(s)."));
    }

    // ---------------- Student ----------------

    @Test
    void student_create_edit_delete() throws Exception {
        mvc.perform(post("/students").param("name", "Test Student").param("email", "Test.Student@ppsu.example")
                        .param("branch", "ce").param("cgpa", "8.25"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/students"));
        mvc.perform(get("/students"))
                .andExpect(content().string(containsString("test.student@ppsu.example")))   // email is normalised
                .andExpect(content().string(containsString("8.25")));

        // seeded ids 1-8, new student is 9
        mvc.perform(post("/students/9").param("name", "Renamed").param("email", "test.student@ppsu.example")
                        .param("branch", "IT").param("cgpa", "9.00"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/students/9/edit")).andExpect(content().string(containsString("Renamed")));

        mvc.perform(post("/students/9/delete")).andExpect(flash().attribute("message", "Student deleted"));
    }

    @Test
    void student_validation_rejects_bad_email_and_cgpa() throws Exception {
        mvc.perform(post("/students").param("name", "A").param("email", "not-an-email")
                        .param("branch", "CE").param("cgpa", "11"))
                .andExpect(status().isOk()).andExpect(view().name("students/form"))
                .andExpect(content().string(containsString("Enter a valid email")))
                .andExpect(content().string(containsString("CGPA must be between 0 and 10")));
    }

    @Test
    void student_duplicate_email_and_student_with_applications() throws Exception {
        mvc.perform(post("/students").param("name", "Clone").param("email", "ASHA@ppsu.example")
                        .param("branch", "CE").param("cgpa", "7"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("already exists")));
        mvc.perform(post("/students/1/delete"))
                .andExpect(flash().attribute("error", "Cannot delete Asha Patel: 3 application(s) exist."));
    }

    // ---------------- Job posting ----------------

    @Test
    void job_create_edit_delete() throws Exception {
        mvc.perform(post("/jobs").param("companyId", "5").param("title", "Crud Test Job")
                        .param("minPackageLpa", "3.00").param("maxPackageLpa", "4.50")
                        .param("lastDate", "2027-01-15").param("status", "OPEN"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/jobs")).andExpect(content().string(containsString("Crud Test Job")));

        // seeded ids 1-12, new job is 13
        mvc.perform(post("/jobs/13").param("companyId", "5").param("title", "Crud Test Job v2")
                        .param("minPackageLpa", "3.00").param("maxPackageLpa", "5.00")
                        .param("lastDate", "2027-01-15").param("status", "CLOSED"))
                .andExpect(redirectedUrl("/jobs/13"));
        mvc.perform(get("/jobs/13")).andExpect(content().string(containsString("Crud Test Job v2")));
        mvc.perform(get("/jobs")).andExpect(content().string(org.hamcrest.Matchers.not(containsString("Crud Test Job v2"))));

        mvc.perform(post("/jobs/13/delete")).andExpect(flash().attribute("message", "Job posting deleted"));
        mvc.perform(get("/jobs/13")).andExpect(status().isNotFound());
    }

    @Test
    void job_rules_package_range_duplicate_title_and_applications() throws Exception {
        mvc.perform(post("/jobs").param("companyId", "5").param("title", "Range Job")
                        .param("minPackageLpa", "6").param("maxPackageLpa", "4")
                        .param("lastDate", "2027-01-15").param("status", "OPEN"))
                .andExpect(status().isOk()).andExpect(view().name("jobs/form"))
                .andExpect(content().string(containsString("Maximum package must not be lower")));
        mvc.perform(post("/jobs").param("companyId", "5").param("title", "java backend intern")
                        .param("minPackageLpa", "3").param("maxPackageLpa", "4")
                        .param("lastDate", "2027-01-15").param("status", "OPEN"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("already exists")));
        mvc.perform(post("/jobs").param("companyId", "").param("title", "")
                        .param("lastDate", "").param("status", "OPEN"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Choose a company")));
        mvc.perform(post("/jobs/1/delete"))
                .andExpect(flash().attributeExists("error"));
    }

    // ---------------- Application status ----------------

    @Test
    void status_form_with_stale_version_gives_409_page() throws Exception {
        // application 20 (Yash / Technical Support Associate) starts at version 0
        mvc.perform(post("/applications/20/status").param("status", "SELECTED").param("version", "0"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/applications/20/status").param("status", "REJECTED").param("version", "0"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("conflicts with the current data")));
        mvc.perform(get("/applications/20/status")).andExpect(status().isOk())
                .andExpect(content().string(containsString("SELECTED")));
    }

    @Test
    void withdraw_removes_the_application() throws Exception {
        mvc.perform(post("/applications/19/delete")).andExpect(flash().attribute("message", "Application withdrawn"));
        mvc.perform(get("/applications/19/status")).andExpect(status().isNotFound());
    }
}
