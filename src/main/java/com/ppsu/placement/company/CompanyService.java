package com.ppsu.placement.company;

import com.ppsu.placement.common.ConflictException;
import com.ppsu.placement.common.NotFoundException;
import com.ppsu.placement.job.JobRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    private final JobRepository jobs;

    public CompanyService(CompanyRepository companies, JobRepository jobs) {
        this.companies = companies;
        this.jobs = jobs;
    }

    @Transactional(readOnly = true)
    public List<CompanyView> list() {
        return companies.findAllWithJobCount();
    }

    @Transactional(readOnly = true)
    public CompanyForm getForm(Long id) {
        Company c = find(id);
        return new CompanyForm(c.getName(), c.getCity(), c.getWebsite());
    }

    @Transactional
    public Long create(CompanyForm form) {
        String name = form.name().trim();
        if (companies.existsByNameIgnoreCase(name))
            throw new ConflictException("A company named \"" + name + "\" already exists.");
        return companies.save(new Company(name, form.city().trim(), blankToNull(form.website()))).getId();
    }

    @Transactional
    public void update(Long id, CompanyForm form) {
        Company c = find(id);
        String name = form.name().trim();
        if (companies.existsByNameIgnoreCaseAndIdNot(name, id))
            throw new ConflictException("A company named \"" + name + "\" already exists.");
        c.setName(name);                          // no save(): dirty checking writes the UPDATE
        c.setCity(form.city().trim());
        c.setWebsite(blankToNull(form.website()));
    }

    @Transactional
    public void delete(Long id) {
        Company c = find(id);
        long postings = jobs.countByCompanyId(id);
        if (postings > 0)
            throw new ConflictException("Cannot delete " + c.getName() + ": it still has " + postings + " job posting(s).");
        companies.delete(c);
    }

    private Company find(Long id) {
        return companies.findById(id).orElseThrow(() -> new NotFoundException("Company " + id));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
