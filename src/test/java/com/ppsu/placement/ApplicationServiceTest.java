package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ppsu.placement.application.Application;
import com.ppsu.placement.application.ApplicationRepository;
import com.ppsu.placement.application.ApplicationService;
import com.ppsu.placement.application.ApplicationStatus;
import com.ppsu.placement.application.StatusForm;
import com.ppsu.placement.common.DuplicateApplicationException;
import com.ppsu.placement.common.JobClosedException;
import com.ppsu.placement.common.NotFoundException;
import com.ppsu.placement.job.JobRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

/** Business rules and transactions: apply guards, dirty checking, rollback, optimistic locking. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:svc;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@ActiveProfiles("local")
class ApplicationServiceTest {

    @Autowired ApplicationService service;
    @Autowired ApplicationRepository applications;
    @Autowired JobRepository jobs;

    private Long jobId(String title) {
        return jobs.findAll().stream().filter(j -> j.getTitle().equals(title)).findFirst().orElseThrow().getId();
    }

    private Long applicationIdOf(String student, String job) {
        return service.listFast().stream()
                .filter(r -> r.student().equals(student) && r.job().equals(job)).findFirst().orElseThrow().id();
    }

    @Test
    void apply_creates_an_application_and_rejects_a_second_one() {
        Long job = jobId("Data Analyst Intern");
        Application a = service.apply(job, "yash@ppsu.example");
        assertThat(a.getId()).isNotNull();
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.APPLIED);

        assertThatThrownBy(() -> service.apply(job, "yash@ppsu.example"))
                .isInstanceOf(DuplicateApplicationException.class);
        service.withdraw(a.getId());                       // leave the data as we found it
    }

    @Test
    void apply_to_a_closed_job_is_rejected() {
        Long closed = jobId("QA Automation Trainee");
        assertThatThrownBy(() -> service.apply(closed, "yash@ppsu.example")).isInstanceOf(JobClosedException.class);
    }

    @Test
    void apply_with_unknown_email_or_job_is_not_found() {
        assertThatThrownBy(() -> service.apply(jobId("DevOps Trainee"), "nobody@ppsu.example"))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.apply(9999L, "asha@ppsu.example")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shortlist_updates_the_row_without_calling_save() {      // DEMO 5: dirty checking
        Long id = applicationIdOf("Dev Trivedi", "Cloud Support Engineer");
        service.shortlist(id);
        assertThat(applications.findById(id).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        service.changeStatus(new StatusForm(id, ApplicationStatus.APPLIED, applications.findById(id).orElseThrow().getVersion()));
    }

    @Test
    void bulkShortlist_rolls_everything_back_when_one_id_is_bad() {
        Long first = applicationIdOf("Yash Gandhi", "Technical Support Associate");
        Long second = applicationIdOf("Nisha Parmar", "Data Analyst Intern");

        assertThatThrownBy(() -> service.bulkShortlist(List.of(first, second, 9999L)))
                .isInstanceOf(NotFoundException.class);

        assertThat(applications.findById(first).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(applications.findById(second).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void stale_form_version_is_rejected_by_optimistic_locking() {
        Long id = applicationIdOf("Kabir Mehta", "Mobile App Trainee");
        Long staleVersion = applications.findById(id).orElseThrow().getVersion();

        service.changeStatus(new StatusForm(id, ApplicationStatus.SHORTLISTED, staleVersion));   // tab A: works
        assertThatThrownBy(() -> service.changeStatus(new StatusForm(id, ApplicationStatus.REJECTED, staleVersion)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);                    // tab B: stale

        Application now = applications.findById(id).orElseThrow();
        assertThat(now.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        assertThat(now.getVersion()).isEqualTo(staleVersion + 1);
        service.changeStatus(new StatusForm(id, ApplicationStatus.APPLIED, now.getVersion()));
    }
}
