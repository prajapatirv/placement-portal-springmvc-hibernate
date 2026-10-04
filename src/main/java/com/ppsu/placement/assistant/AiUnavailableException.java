package com.ppsu.placement.assistant;

public class AiUnavailableException extends Exception {
    public AiUnavailableException(String message) { super(message); }
    public AiUnavailableException(String message, Throwable cause) { super(message, cause); }
}
