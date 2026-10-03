package com.ppsu.placement.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Example M4: the same apply rule as the HTML form, now as JSON with 201 Created and a Location header. */
@RestController
@RequestMapping("/api/applications")
class ApplicationApiController {
    private final ApplicationService applications;

    ApplicationApiController(ApplicationService applications) {
        this.applications = applications;
    }

    record ApplyRequest(@NotNull Long jobId, @NotBlank @Email String email) {}

    @PostMapping
    ResponseEntity<ApplicationRow> apply(@Valid @RequestBody ApplyRequest request) {
        Application saved = applications.apply(request.jobId(), request.email());
        ApplicationRow row = applications.get(saved.getId());
        return ResponseEntity.created(URI.create("/api/applications/" + saved.getId())).body(row);
    }

    @GetMapping("/{id}")
    ApplicationRow one(@PathVariable Long id) {
        return applications.get(id);
    }

    /** Lets the demo be repeated: withdraw what the POST just created. */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> withdraw(@PathVariable Long id) {
        applications.withdraw(id);
        return ResponseEntity.noContent().build();
    }
}
