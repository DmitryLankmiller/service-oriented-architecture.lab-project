package ru.ifmo.se.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.se.dto.GreetingDto;

@RestController
public class HellCoWorldController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public GreetingDto greeting(@RequestParam(defaultValue = "World") String name) {
        return new GreetingDto(counter.incrementAndGet(), template.formatted(name));
    }
}