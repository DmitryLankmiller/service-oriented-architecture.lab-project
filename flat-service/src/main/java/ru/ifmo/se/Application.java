package ru.ifmo.se;

import io.micronaut.runtime.Micronaut;
import io.github.cdimascio.dotenv.Dotenv;

public class Application {

    public static void main(String[] args) {
        Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        Micronaut.run(Application.class, args);
    }
}