package com.ppsu.placement.application;

import com.ppsu.placement.job.JobService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ApplicationController {
    private final ApplicationService applications;
    private final JobService jobs;

    public ApplicationController(ApplicationService applications, JobService jobs) {
        this.applications = applications;
        this.jobs = jobs;
    }

    @GetMapping("/applications")
    public String list(@RequestParam(defaultValue = "false") boolean slow, Model model) {
        model.addAttribute("rows", slow ? applications.listSlow() : applications.listFast());
        model.addAttribute("slow", slow);
        return "applications/list";
    }

    @GetMapping("/applications/mine")
    public String mine(@RequestParam String email, @RequestParam(defaultValue = "false") boolean slow, Model model) {
        model.addAttribute("rows", applications.mine(email, slow));
        model.addAttribute("slow", slow);
        model.addAttribute("mineEmail", email);
        return "applications/list";                            // reuse the existing template
    }

    // ---- apply (Post-Redirect-Get) ----

    @GetMapping("/jobs/{id}/apply")
    public String applyForm(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobs.getJob(id));
        model.addAttribute("form", new ApplyForm(""));
        return "apply";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, @Valid @ModelAttribute("form") ApplyForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("job", jobs.getJob(id));
            return "apply";                                    // redisplay with the error messages
        }
        applications.apply(id, form.email());
        redirect.addFlashAttribute("message", "Application submitted");
        return "redirect:/applications";                       // Post-Redirect-Get
    }

    // ---- status changes ----

    @PostMapping("/applications/{id}/shortlist")
    public String shortlist(@PathVariable Long id, RedirectAttributes redirect) {
        applications.shortlist(id);
        redirect.addFlashAttribute("message", "Application " + id + " shortlisted");
        return "redirect:/applications";
    }

    @PostMapping("/applications/bulk-shortlist")
    public String bulkShortlist(@RequestParam("ids") List<Long> ids, RedirectAttributes redirect) {
        applications.bulkShortlist(ids);
        redirect.addFlashAttribute("message", ids.size() + " applications shortlisted");
        return "redirect:/applications";
    }

    @GetMapping("/applications/{id}/status")
    public String statusForm(@PathVariable Long id, Model model) {
        ApplicationRow row = applications.get(id);
        model.addAttribute("row", row);
        model.addAttribute("form", new StatusForm(row.id(), row.status(), row.version()));
        model.addAttribute("statuses", ApplicationStatus.values());
        return "applications/status";
    }

    @PostMapping("/applications/{id}/status")
    public String changeStatus(@PathVariable Long id, @Valid @ModelAttribute("form") StatusForm form,
                               BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("row", applications.get(id));
            model.addAttribute("statuses", ApplicationStatus.values());
            return "applications/status";
        }
        applications.changeStatus(new StatusForm(id, form.status(), form.version()));
        redirect.addFlashAttribute("message", "Status updated");
        return "redirect:/applications";
    }

    @PostMapping("/applications/{id}/delete")
    public String withdraw(@PathVariable Long id, RedirectAttributes redirect) {
        applications.withdraw(id);
        redirect.addFlashAttribute("message", "Application withdrawn");
        return "redirect:/applications";
    }
}
