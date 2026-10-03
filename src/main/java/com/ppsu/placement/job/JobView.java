package com.ppsu.placement.job;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Flat read model: controllers and templates never see entities. */
public record JobView(Long id, String title, Long companyId, String company, String city,
                      BigDecimal minLpa, BigDecimal maxLpa, LocalDate lastDate, JobStatus status) {

    /** Example M2: the rule lives in Java, where it can be tested; the template only asks. */
    public boolean closingSoon() {
        LocalDate today = LocalDate.now();
        return !lastDate.isBefore(today) && !lastDate.isAfter(today.plusDays(3));
    }
}
