package com.ppsu.placement.assistant;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Add, list and remove runtime keyword rules. Same no-login caveat as the assistant page. */
@RestController
@RequestMapping("/admin/assistant/rules")
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class RulesController {

    public record NewRule(String keyword, String tool, String status) {}

    private final KeywordRules rules;

    public RulesController(KeywordRules rules) { this.rules = rules; }

    @GetMapping
    public List<KeywordRules.Rule> list() { return rules.all(); }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody NewRule body) {
        try {
            return ResponseEntity.ok(rules.add(body.keyword(), body.tool(), body.status()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestParam String keyword) {
        return rules.remove(keyword) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
