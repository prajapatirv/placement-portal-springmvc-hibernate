package com.ppsu.placement.common;

/** Thrown when a requested record does not exist; mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String what) {
        super(what + " was not found");
    }
}
