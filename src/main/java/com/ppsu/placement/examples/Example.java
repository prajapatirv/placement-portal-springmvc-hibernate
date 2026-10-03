package com.ppsu.placement.examples;

import java.util.ArrayList;
import java.util.List;

/** One session example: what to say, what to show, what to run. Public fields are read by the Thymeleaf pages. */
public class Example {
    /** A code block shown on the page. */
    public record Snippet(String title, String lang, String code) {}

    /** One step of the "how it works inside" section: a heading, some prose, and optional code or SQL. */
    public record Block(String heading, String text, String lang, String code) {}

    /** A button on the page. {@code status} is the HTTP status the audience should see; the smoke test checks it. */
    public record Run(String label, String method, String url, String body, String contentType, int status,
                      String expect, boolean mutates) {}

    public final String id;
    public final String track;            // "Hibernate" or "Spring MVC"
    public final int level;               // 1 basics ... 4 advanced (optional)
    public final String title;
    public String reallife = "";
    public String slides = "";
    public String say = "";
    public String predict = "";
    public String reveal = "";
    public String breakIt = "";
    public String watch = "";
    public final List<String> files = new ArrayList<>();
    public final List<Snippet> snippets = new ArrayList<>();
    public final List<Block> inside = new ArrayList<>();
    public final List<Run> runs = new ArrayList<>();

    Example(String id, String track, int level, String title) {
        this.id = id;
        this.track = track;
        this.level = level;
        this.title = title;
    }

    Example reallife(String s) { this.reallife = s; return this; }
    Example slides(String s) { this.slides = s; return this; }
    Example say(String s) { this.say = s; return this; }
    Example predict(String question, String answer) { this.predict = question; this.reveal = answer; return this; }
    Example breakIt(String s) { this.breakIt = s; return this; }
    Example watch(String s) { this.watch = s; return this; }
    Example inside(String heading, String text, String lang, String code) {
        inside.add(new Block(heading, text, lang, code == null ? "" : code.strip()));
        return this;
    }
    Example files(String... f) { this.files.addAll(List.of(f)); return this; }
    Example code(String title, String lang, String code) { snippets.add(new Snippet(title, lang, code.strip())); return this; }

    Example get(String label, String url, int status, String expect) {
        runs.add(new Run(label, "GET", url, null, null, status, expect, false));
        return this;
    }

    Example post(String label, String url, String body, String contentType, int status, String expect, boolean mutates) {
        runs.add(new Run(label, "POST", url, body, contentType, status, expect, mutates));
        return this;
    }

    public String trackCode() {
        return track.startsWith("H") ? "H" : "M";
    }

    public boolean optional() {
        return level >= 4;
    }
}
