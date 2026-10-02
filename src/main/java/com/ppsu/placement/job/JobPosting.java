package com.ppsu.placement.job;

import com.ppsu.placement.company.Company;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "job_posting")
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)   // owning side, LAZY on purpose
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "min_package_lpa", nullable = false)
    private BigDecimal minPackageLpa;

    @Column(name = "max_package_lpa", nullable = false)
    private BigDecimal maxPackageLpa;

    @Column(name = "last_date", nullable = false)
    private LocalDate lastDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    protected JobPosting() {}

    public JobPosting(Company company, String title, BigDecimal minPackageLpa, BigDecimal maxPackageLpa,
                      LocalDate lastDate, JobStatus status) {
        this.company = company;
        this.title = title;
        this.minPackageLpa = minPackageLpa;
        this.maxPackageLpa = maxPackageLpa;
        this.lastDate = lastDate;
        this.status = status;
    }

    /** Business rule lives on the entity: a posting accepts applications while OPEN and not past its last date. */
    public boolean isOpenOn(LocalDate day) {
        return status == JobStatus.OPEN && !day.isAfter(lastDate);
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getMinPackageLpa() { return minPackageLpa; }
    public void setMinPackageLpa(BigDecimal minPackageLpa) { this.minPackageLpa = minPackageLpa; }
    public BigDecimal getMaxPackageLpa() { return maxPackageLpa; }
    public void setMaxPackageLpa(BigDecimal maxPackageLpa) { this.maxPackageLpa = maxPackageLpa; }
    public LocalDate getLastDate() { return lastDate; }
    public void setLastDate(LocalDate lastDate) { this.lastDate = lastDate; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
}
