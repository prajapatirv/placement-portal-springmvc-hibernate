package com.ppsu.placement.job;

import com.ppsu.placement.application.ApplicationRepository;
import com.ppsu.placement.common.ConflictException;
import com.ppsu.placement.common.NotFoundException;
import com.ppsu.placement.company.Company;
import com.ppsu.placement.company.CompanyRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {
    private final JobRepository jobs;
    private final CompanyRepository companies;
    private final ApplicationRepository applications;

    public JobService(JobRepository jobs, CompanyRepository companies, ApplicationRepository applications) {
        this.jobs = jobs;
        this.companies = companies;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<JobView> listOpenJobs() {
        return listOpenJobs(null);
    }

    @Transactional(readOnly = true)
    public List<JobView> listOpenJobs(String city) {
        var found = (city == null || city.isBlank())
                ? jobs.findOpenWithCompany() : jobs.findOpenByCity(city.trim());
        return found.stream().map(this::toView).toList();
    }

    /** Admin list of every posting, paged: ten rows, not a million. */
    @Transactional(readOnly = true)
    public Page<JobView> page(int page, int size) {
        return jobs.findAllBy(PageRequest.of(Math.max(page, 0), size, Sort.by("lastDate", "id")))
                .map(this::toView);
    }

    @Transactional(readOnly = true)
    public JobView getJob(Long id) {
        return toView(jobs.findWithCompanyById(id).orElseThrow(() -> new NotFoundException("Job " + id)));
    }

    @Transactional(readOnly = true)
    public List<JobSummary> summaries() {
        return jobs.summaries(JobStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<JobApplicantCount> popular() {                    // example H4
        return jobs.applicantsPerJob();
    }

    @Transactional(readOnly = true)
    public List<JobView> paying(BigDecimal minLpa) {              // example M1
        return jobs.findByMinPackageLpaGreaterThanEqualOrderByMinPackageLpaDesc(minLpa)
                .stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public JobForm getForm(Long id) {
        JobView j = getJob(id);
        return new JobForm(j.companyId(), j.title(), j.minLpa(), j.maxLpa(), j.lastDate(), j.status());
    }

    @Transactional(readOnly = true)
    public List<CompanyOption> companyOptions() {
        return companies.findAll(Sort.by("name")).stream()
                .map(c -> new CompanyOption(c.getId(), c.getName())).toList();
    }

    @Transactional
    public Long create(JobForm form) {
        validate(form, null);
        Company company = companies.findById(form.companyId())
                .orElseThrow(() -> new NotFoundException("Company " + form.companyId()));
        return jobs.save(new JobPosting(company, form.title().trim(), form.minPackageLpa(),
                form.maxPackageLpa(), form.lastDate(), form.status())).getId();
    }

    @Transactional
    public void update(Long id, JobForm form) {
        JobPosting job = jobs.findById(id).orElseThrow(() -> new NotFoundException("Job " + id));
        validate(form, id);
        job.setCompany(companies.findById(form.companyId())
                .orElseThrow(() -> new NotFoundException("Company " + form.companyId())));
        job.setTitle(form.title().trim());
        job.setMinPackageLpa(form.minPackageLpa());
        job.setMaxPackageLpa(form.maxPackageLpa());
        job.setLastDate(form.lastDate());
        job.setStatus(form.status());
    }

    @Transactional
    public void delete(Long id) {
        JobPosting job = jobs.findById(id).orElseThrow(() -> new NotFoundException("Job " + id));
        long apps = applications.countByJobId(id);
        if (apps > 0)
            throw new ConflictException("Cannot delete \"" + job.getTitle() + "\": " + apps
                    + " application(s) refer to it. Close the posting instead.");
        jobs.delete(job);
    }

    private void validate(JobForm form, Long selfId) {
        if (form.maxPackageLpa().compareTo(form.minPackageLpa()) < 0)
            throw new ConflictException("Maximum package must not be lower than the minimum package.");
        String title = form.title().trim();
        boolean duplicate = selfId == null
                ? jobs.existsByTitleIgnoreCase(title)
                : jobs.existsByTitleIgnoreCaseAndIdNot(title, selfId);
        if (duplicate) throw new ConflictException("A job posting titled \"" + title + "\" already exists.");
    }

    private JobView toView(JobPosting j) {
        Company c = j.getCompany();
        return new JobView(j.getId(), j.getTitle(), c.getId(), c.getName(), c.getCity(),
                j.getMinPackageLpa(), j.getMaxPackageLpa(), j.getLastDate(), j.getStatus());
    }
}
