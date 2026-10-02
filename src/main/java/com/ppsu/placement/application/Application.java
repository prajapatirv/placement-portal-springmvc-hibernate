package com.ppsu.placement.application;

import com.ppsu.placement.job.JobPosting;
import com.ppsu.placement.student.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Association entity: links a student and a posting, and carries its own data (status, time, version). */
@Entity
@Table(name = "application",
       uniqueConstraints = @UniqueConstraint(name = "uq_application", columnNames = {"student_id", "job_id"}))
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private JobPosting job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Version                                   // optimistic locking
    private Long version;

    protected Application() {}

    public Application(Student student, JobPosting job) {
        this.student = student;
        this.job = job;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public JobPosting getJob() { return job; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public Long getVersion() { return version; }
}
