package com.ppsu.placement.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController {
    private final DashboardService dashboard;

    HomeController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/")
    String home(Model model) {
        model.addAttribute("counts", dashboard.counts());
        return "home";
    }
}
