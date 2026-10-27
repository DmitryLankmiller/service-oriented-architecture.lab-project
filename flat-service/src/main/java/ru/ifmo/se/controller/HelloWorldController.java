package ru.ifmo.se.controller;

import java.util.concurrent.atomic.AtomicLong;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.server.cors.CrossOrigin;
import ru.ifmo.se.dto.GreetingDto;

@Controller
public class HelloWorldController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Get("/greeting")
    @CrossOrigin(allowedOrigins = "*")
    public GreetingDto greeting(@QueryValue(defaultValue = "World") String name) {
        return new GreetingDto(counter.incrementAndGet(), template.formatted(name));
    }
}
