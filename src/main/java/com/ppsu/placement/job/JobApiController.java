package com.ppsu.placement.job;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Same data as the HTML pages, now as JSON. */
@RestController
@RequestMapping("/api/jobs")
class JobApiController {
    private final JobService jobs;

    JobApiController(JobService jobs) {
        this.jobs = jobs;
    }

    @GetMapping
    List<JobView> all(@RequestParam(required = false) String city) {
        return jobs.listOpenJobs(city);
    }

    @GetMapping("/popular")
    List<JobApplicantCount> popular() {
        return jobs.popular();
    }

    @GetMapping("/paying")
    List<JobView> paying(@RequestParam BigDecimal minLpa) {
        return jobs.paying(minLpa);
    }

    @GetMapping("/{id}")
    JobView one(@PathVariable Long id) {
        return jobs.getJob(id);
    }

    @GetMapping("/summaries")
    List<JobSummaryDto> summaries() {
        return jobs.summaries().stream().map(s -> new JobSummaryDto(s.getTitle(), s.getCompanyName())).toList();
    }

    record JobSummaryDto(String title, String company) {}
}
