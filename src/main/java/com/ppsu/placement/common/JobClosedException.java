package com.ppsu.placement.common;

public class JobClosedException extends ConflictException {
    public JobClosedException(String title) {
        super("Applications for \"" + title + "\" are closed.");
    }
}
