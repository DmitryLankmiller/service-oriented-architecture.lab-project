package ru.ifmo.se.exception;

import lombok.Getter;

@Getter
public class InvalidQueryException extends RuntimeException {
    private final String field;

    public InvalidQueryException(String field, String message) {
        super(message);
        this.field = field;
    }
}
