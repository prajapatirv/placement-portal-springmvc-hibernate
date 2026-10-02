package com.ppsu.placement.common;

public class DuplicateApplicationException extends ConflictException {
    public DuplicateApplicationException() {
        super("This student has already applied to this job posting.");
    }
}
