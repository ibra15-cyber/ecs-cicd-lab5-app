package com.ibra.simple_full_stack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
    
    @GetMapping("/env")
    public String environment() {
        return "Database URL: " + System.getenv("SPRING_DATASOURCE_URL");
    }
}