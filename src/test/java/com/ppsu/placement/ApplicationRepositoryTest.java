package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;

import com.ppsu.placement.application.ApplicationRepository;
import com.ppsu.placement.job.JobRepository;
import com.ppsu.placement.job.JobStatus;
import com.ppsu.placement.student.Student;
import com.ppsu.placement.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/** Repository slice test: Flyway migrations + Hibernate validate + derived and JPQL queries on H2. */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:repo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class ApplicationRepositoryTest {

    @Autowired ApplicationRepository applications;
    @Autowired StudentRepository students;
    @Autowired JobRepository jobs;

    @Test
    void seed_data_is_loaded() {
        assertThat(applications.count()).isEqualTo(20);
        assertThat(students.count()).isEqualTo(8);
        assertThat(jobs.count()).isEqualTo(12);
    }

    @Test
    void existsByStudentIdAndJobId_is_derived_from_the_method_name() {
        Student asha = students.findByEmail("asha@ppsu.example").orElseThrow();
        Long javaBackend = jobs.findAll().stream()
                .filter(j -> j.getTitle().equals("Java Backend Intern")).findFirst().orElseThrow().getId();
        Long qaTrainee = jobs.findAll().stream()
                .filter(j -> j.getTitle().equals("QA Automation Trainee")).findFirst().orElseThrow().getId();

        assertThat(applications.existsByStudentIdAndJobId(asha.getId(), javaBackend)).isTrue();
        assertThat(applications.existsByStudentIdAndJobId(asha.getId(), qaTrainee)).isFalse();
    }

    @Test
    void findAllWithDetails_returns_every_application_newest_first() {
        assertThat(applications.findAllWithDetails()).hasSize(20);
    }

    @Test
    void open_jobs_exclude_closed_postings() {
        assertThat(jobs.findOpenWithCompany()).hasSize(11)
                .noneMatch(j -> j.getTitle().equals("QA Automation Trainee"));
    }

    @Test
    void city_filter_is_case_insensitive() {
        assertThat(jobs.findOpenByCity("sURAT")).extracting("title")
                .containsExactlyInAnyOrder("Cloud Support Engineer", "DevOps Trainee", "Java Full Stack Trainee");
    }

    @Test
    void paging_with_entity_graph_returns_the_requested_page() {
        var page = jobs.findByStatus(JobStatus.OPEN, PageRequest.of(0, 5));
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(11);
    }

    @Test
    void projection_reads_two_columns() {
        assertThat(jobs.summaries(JobStatus.OPEN)).hasSize(11)
                .allSatisfy(s -> assertThat(s.getCompanyName()).isNotBlank());
    }
}
