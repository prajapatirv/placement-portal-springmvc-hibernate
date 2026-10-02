package com.ppsu.placement.company;

import com.ppsu.placement.common.ConflictException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/companies")
public class CompanyController {
    private final CompanyService companies;

    public CompanyController(CompanyService companies) {
        this.companies = companies;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companies.list());
        return "companies/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", CompanyForm.empty());
        return formPage(model, "/companies", "Add company");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CompanyForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                companies.create(form);
                redirect.addFlashAttribute("message", "Company created");
                return "redirect:/companies";
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/companies", "Add company");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", companies.getForm(id));
        return formPage(model, "/companies/" + id, "Edit company");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CompanyForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                companies.update(id, form);
                redirect.addFlashAttribute("message", "Company updated");
                return "redirect:/companies";
            } catch (ConflictException e) {
                result.reject("conflict", e.getMessage());
            }
        }
        return formPage(model, "/companies/" + id, "Edit company");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            companies.delete(id);
            redirect.addFlashAttribute("message", "Company deleted");
        } catch (ConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/companies";
    }

    private String formPage(Model model, String action, String heading) {
        model.addAttribute("action", action);
        model.addAttribute("heading", heading);
        return "companies/form";
    }
}
