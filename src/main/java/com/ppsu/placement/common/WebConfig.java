package com.ppsu.placement.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebConfig implements WebMvcConfigurer {
    private final QueryCountInterceptor queryCount;

    WebConfig(QueryCountInterceptor queryCount) {
        this.queryCount = queryCount;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queryCount).excludePathPatterns(
                "/css/**", "/img/**", "/favicon.ico", "/actuator/**",
                "/examples", "/examples/*", "/examples/requests/**", "/demo", "/demo/**");   // the guide itself is not a demo request
    }
}
