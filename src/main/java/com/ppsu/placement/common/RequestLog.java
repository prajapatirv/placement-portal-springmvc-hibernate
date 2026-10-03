package com.ppsu.placement.common;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Component;

/** The last few requests with their SQL count, so the examples page can show what the console shows. */
@Component
public class RequestLog {
    public record Entry(String time, String method, String uri, int status, long sql, long ms) {}

    private static final int MAX = 15;
    private final Deque<Entry> entries = new ArrayDeque<>();

    synchronized void add(String method, String uri, int status, long sql, long ms) {
        if (entries.size() == MAX) entries.removeLast();
        entries.addFirst(new Entry(LocalTime.now().withNano(0).toString(), method, uri, status, sql, ms));
    }

    /** Newest first. */
    public synchronized List<Entry> recent() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
    }
}
