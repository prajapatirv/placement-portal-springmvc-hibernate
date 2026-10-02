package com.ppsu.placement.application;

import java.time.LocalDateTime;

/** Flat record for the applications page; carries the version so a stale form can be detected. */
public record ApplicationRow(Long id, String student, String job, String company,
                             ApplicationStatus status, LocalDateTime appliedAt, Long version) {}
