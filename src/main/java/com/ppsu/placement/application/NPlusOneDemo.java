package com.ppsu.placement.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Switched on with --demo.n-plus-one=true; compare the Session Metrics lines of the two runs. */
@Component
@ConditionalOnProperty(name = "demo.n-plus-one", havingValue = "true")
class NPlusOneDemo implements ApplicationRunner {
    private final ApplicationService service;

    NPlusOneDemo(ApplicationService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("=== SLOW: findAll() with LAZY associations ===");
        service.listSlow();
        System.out.println("=== FAST: one JOIN FETCH query ===");
        service.listFast();
    }
}
