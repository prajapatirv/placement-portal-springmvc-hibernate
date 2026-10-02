package com.ppsu.placement.application;

import com.ppsu.placement.common.DuplicateApplicationException;
import com.ppsu.placement.common.JobClosedException;
import com.ppsu.placement.common.NotFoundException;
import com.ppsu.placement.job.JobPosting;
import com.ppsu.placement.job.JobRepository;
import com.ppsu.placement.student.Student;
import com.ppsu.placement.student.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationRepository applications;
    private final JobRepository jobs;
    private final StudentRepository students;

    public ApplicationService(ApplicationRepository applications, JobRepository jobs, StudentRepository students) {
        this.applications = applications;
        this.jobs = jobs;
        this.students = students;
    }

    @Transactional(readOnly = true)
    public List<ApplicationRow> listSlow() {          // 26 queries with the seed data
        return applications.findAll().stream().map(this::toRow).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationRow> listFast() {          // 1 query
        return applications.findAllWithDetails().stream().map(this::toRow).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationRow get(Long id) {
        return toRow(applications.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Application " + id)));
    }

    @Transactional
    public Application apply(Long jobId, String email) {
        JobPosting job = jobs.findById(jobId).orElseThrow(() -> new NotFoundException("Job " + jobId));
        if (!job.isOpenOn(LocalDate.now())) throw new JobClosedException(job.getTitle());
        Student student = students.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Student " + email));
        if (applications.existsByStudentIdAndJobId(student.getId(), jobId))
            throw new DuplicateApplicationException();
        return applications.save(new Application(student, job));
    }

    @Transactional
    public void shortlist(Long applicationId) {       // Demo 5: no save() call anywhere
        Application a = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application " + applicationId));
        a.setStatus(ApplicationStatus.SHORTLISTED);
    }

    /** All or nothing: one bad id rolls back every change made earlier in the loop. */
    @Transactional
    public void bulkShortlist(List<Long> ids) {
        for (Long id : ids) {
            Application a = applications.findById(id)
                    .orElseThrow(() -> new NotFoundException("Application " + id));
            a.setStatus(ApplicationStatus.SHORTLISTED);
        }
    }

    /** Optimistic locking across a browser form that may be minutes old. */
    @Transactional
    public void changeStatus(StatusForm form) {
        Application a = applications.findById(form.id())
                .orElseThrow(() -> new NotFoundException("Application " + form.id()));
        if (!a.getVersion().equals(form.version()))
            throw new ObjectOptimisticLockingFailureException(Application.class, form.id());
        a.setStatus(form.status());                  // flush: update ... where id=? and version=?
    }

    @Transactional
    public void withdraw(Long id) {
        Application a = applications.findById(id).orElseThrow(() -> new NotFoundException("Application " + id));
        applications.delete(a);
    }

    private ApplicationRow toRow(Application a) {
        return new ApplicationRow(a.getId(), a.getStudent().getName(), a.getJob().getTitle(),
                a.getJob().getCompany().getName(), a.getStatus(), a.getAppliedAt(), a.getVersion());
    }
}
