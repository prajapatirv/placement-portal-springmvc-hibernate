package com.ppsu.placement.job;

/** Interface projection: Hibernate selects only these two columns (L6 performance toolbox). */
public interface JobSummary {
    String getTitle();

    String getCompanyName();
}
