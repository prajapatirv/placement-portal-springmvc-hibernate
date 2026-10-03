package com.ppsu.placement.examples;

import com.ppsu.placement.common.NotFoundException;
import com.ppsu.placement.common.RequestLog;
import com.ppsu.placement.job.JobPosting;
import com.ppsu.placement.job.JobRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The session guide: /examples is the index (appendix), /examples/{id} is one example with its code, a talk track,
 * Run buttons and a live request log. /examples/run/** are the runnable demos that do not fit an existing page.
 */
@Controller
public class ExamplesController {
    private final ExampleCatalog catalog;
    private final ExampleLab lab;
    private final RequestLog requestLog;
    private final JobRepository jobRepository;

    ExamplesController(ExampleCatalog catalog, ExampleLab lab, RequestLog requestLog, JobRepository jobRepository) {
        this.catalog = catalog;
        this.lab = lab;
        this.requestLog = requestLog;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/examples")
    public String index(Model model) {
        model.addAttribute("examples", catalog.all());
        model.addAttribute("tracks", List.of("Hibernate", "Spring MVC"));
        return "examples/index";
    }

    @GetMapping("/examples/{id}")
    public String show(@PathVariable String id, Model model) {
        Example example = catalog.find(id).orElseThrow(() -> new NotFoundException("Example " + id));
        model.addAttribute("e", example);
        model.addAttribute("prev", catalog.previous(id).orElse(null));
        model.addAttribute("next", catalog.next(id).orElse(null));
        model.addAttribute("position", catalog.all().indexOf(example) + 1);
        model.addAttribute("total", catalog.all().size());
        return "examples/show";
    }

    // ---- the request log the page polls ----

    @GetMapping("/examples/requests")
    @ResponseBody
    public List<RequestLog.Entry> requests() {
        return requestLog.recent();
    }

    @PostMapping("/examples/requests/clear")
    @ResponseBody
    public Map<String, String> clear() {
        requestLog.clear();
        return Map.of("cleared", "true");
    }

    // ---- runnable demos (each one rolls back, so the data is unchanged) ----

    @GetMapping("/examples/run/h1/mapping")
    @ResponseBody
    public Map<String, Object> mapping() {
        return lab.mapping();
    }

    @GetMapping("/examples/run/h6/lifecycle")
    @ResponseBody
    public Map<String, Object> lifecycle() {
        return lab.lifecycle();
    }

    @GetMapping("/examples/run/h6/dirty-checking")
    @ResponseBody
    public Map<String, Object> dirtyChecking() {
        return lab.dirtyChecking();
    }

    @GetMapping("/examples/run/h6/first-level-cache")
    @ResponseBody
    public Map<String, Object> firstLevelCache() {
        return lab.firstLevelCache();
    }

    @GetMapping("/examples/run/h8/compare")
    @ResponseBody
    public Map<String, Object> bulkCompare() {
        return lab.bulkCompare();
    }

    /** Deliberately wrong (example M7): returns entities, not DTOs. Expect a 500. */
    @GetMapping("/examples/run/m7/raw-entity")
    @ResponseBody
    public List<JobPosting> rawEntity() {
        return jobRepository.findAll();
    }
}
