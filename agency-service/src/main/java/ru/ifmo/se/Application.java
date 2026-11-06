package ru.ifmo.se;

import io.github.cdimascio.dotenv.Dotenv;
import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        Micronaut.run(Application.class, args);
    }
}