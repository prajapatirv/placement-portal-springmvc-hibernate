package com.ppsu.placement.common;

/** A business rule was violated; the message is safe to show to the user. Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
