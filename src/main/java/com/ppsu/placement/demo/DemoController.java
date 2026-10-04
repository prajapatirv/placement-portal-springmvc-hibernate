package com.ppsu.placement.demo;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** The Architecture Demo tab: the three-part walkthrough and the flow explorer, shown inside the normal layout. */
@Controller
class DemoController {

    @GetMapping("/demo")
    String demo(Model model) {
        model.addAttribute("groups", List.of(ArtifactLink.WALKTHROUGH, ArtifactLink.PRESENTATION, ArtifactLink.GUIDES));
        model.addAttribute("artifacts", ArtifactLink.ALL);
        return "demo";
    }
}
