package com.ppsu.placement.job;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Flat read model: controllers and templates never see entities. */
public record JobView(Long id, String title, Long companyId, String company, String city,
                      BigDecimal minLpa, BigDecimal maxLpa, LocalDate lastDate, JobStatus status) {}
