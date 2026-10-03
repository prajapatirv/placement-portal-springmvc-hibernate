package com.ppsu.placement.job;

/** Aggregate projection (example H4): one row per job with its number of applicants. */
public interface JobApplicantCount {
    String getTitle();

    Long getApplicants();
}
