package com.ppsu.placement.dashboard;

import com.ppsu.placement.application.ApplicationRepository;
import com.ppsu.placement.company.CompanyRepository;
import com.ppsu.placement.job.JobRepository;
import com.ppsu.placement.student.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    public record Counts(long companies, long jobs, long students, long applications) {}

    private final CompanyRepository companies;
    private final JobRepository jobs;
    private final StudentRepository students;
    private final ApplicationRepository applications;

    public DashboardService(CompanyRepository companies, JobRepository jobs,
                            StudentRepository students, ApplicationRepository applications) {
        this.companies = companies;
        this.jobs = jobs;
        this.students = students;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public Counts counts() {
        return new Counts(companies.count(), jobs.count(), students.count(), applications.count());
    }
}
