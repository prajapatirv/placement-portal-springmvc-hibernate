package com.ppsu.placement.assistant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The page and its JSON endpoint. The demo app has no login, so this is for the classroom only;
 * a real portal would put /admin/** behind Spring Security with an ADMIN role.
 * One switch turns the whole feature off: assistant.enabled=false.
 */
@Controller
@RequestMapping("/admin/assistant")
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class AssistantController {

    public record AskRequest(String question) {}

    private final AssistantService service;

    public AssistantController(AssistantService service) { this.service = service; }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("mode", service.mode());
        return "admin/assistant";                       // templates/admin/assistant.html
    }

    @PostMapping("/ask")
    @ResponseBody
    public AssistantAnswer ask(@RequestBody AskRequest request) {
        return service.ask(request == null ? null : request.question());
    }
}
