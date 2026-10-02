package com.ppsu.placement.job;

import com.ppsu.placement.common.ConflictException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobController {
    private static final int PAGE_SIZE = 5;

    private final JobService jobs;

    public JobController(JobService jobs) {
        this.jobs = jobs;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String city, Model model) {
        model.addAttribute("jobs", jobs.listOpenJobs(city));       // List<JobView>
        model.addAttribute("city", city);
        return "jobs/list";                                         // templates/jobs/list.html
    }

    @GetMapping("/manage")
    public String manage(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<JobView> result = jobs.page(page, PAGE_SIZE);
        model.addAttribute("page", result);
        return "jobs/manage";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobs.getJob(id));
        return "jobs/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", JobForm.empty());
        return formPage(model, "/jobs", "Add job posting");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") JobForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Long id = jobs.create(form);
                redirect.addFlashAttribute("message", "Job posting created");
                return "redirect:/jobs/" + id;
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/jobs", "Add job posting");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", jobs.getForm(id));
        return formPage(model, "/jobs/" + id, "Edit job posting");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") JobForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                jobs.update(id, form);
                redirect.addFlashAttribute("message", "Job posting updated");
                return "redirect:/jobs/" + id;
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/jobs/" + id, "Edit job posting");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            jobs.delete(id);
            redirect.addFlashAttribute("message", "Job posting deleted");
        } catch (ConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jobs/manage";
    }

    private String formPage(Model model, String action, String heading) {
        model.addAttribute("action", action);
        model.addAttribute("heading", heading);
        model.addAttribute("companies", jobs.companyOptions());
        model.addAttribute("statuses", JobStatus.values());
        return "jobs/form";
    }
}
