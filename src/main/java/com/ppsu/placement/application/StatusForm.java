package com.ppsu.placement.application;

import jakarta.validation.constraints.NotNull;

/** Browser form for changing a status; the hidden version field detects a stale page. */
public record StatusForm(Long id, @NotNull(message = "Choose a status") ApplicationStatus status, Long version) {}
