package com.ppsu.placement.job;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record JobForm(
        @NotNull(message = "Choose a company") Long companyId,
        @NotBlank(message = "Title is required") @Size(max = 150) String title,
        @NotNull(message = "Minimum package is required")
        @DecimalMin(value = "0.0", message = "Must be 0 or more") @DecimalMax("999.99") BigDecimal minPackageLpa,
        @NotNull(message = "Maximum package is required")
        @DecimalMin(value = "0.0", message = "Must be 0 or more") @DecimalMax("999.99") BigDecimal maxPackageLpa,
        @NotNull(message = "Last date is required") LocalDate lastDate,
        @NotNull(message = "Status is required") JobStatus status) {

    public static JobForm empty() {
        return new JobForm(null, "", null, null, LocalDate.now().plusMonths(1), JobStatus.OPEN);
    }
}
