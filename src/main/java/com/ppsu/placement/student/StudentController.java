package com.ppsu.placement.student;

import com.ppsu.placement.common.ConflictException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {
    private final StudentService students;

    public StudentController(StudentService students) {
        this.students = students;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String branch,
                       @RequestParam(required = false) BigDecimal minCgpa, Model model) {
        model.addAttribute("students", students.list(branch, minCgpa));
        model.addAttribute("branch", branch);
        model.addAttribute("minCgpa", minCgpa);
        return "students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", StudentForm.empty());
        return formPage(model, "/students", "Add student");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StudentForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                students.create(form);
                redirect.addFlashAttribute("message", "Student created");
                return "redirect:/students";
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/students", "Add student");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", students.getForm(id));
        return formPage(model, "/students/" + id, "Edit student");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                students.update(id, form);
                redirect.addFlashAttribute("message", "Student updated");
                return "redirect:/students";
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/students/" + id, "Edit student");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            students.delete(id);
            redirect.addFlashAttribute("message", "Student deleted");
        } catch (ConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/students";
    }

    private String formPage(Model model, String action, String heading) {
        model.addAttribute("action", action);
        model.addAttribute("heading", heading);
        return "students/form";
    }
}
