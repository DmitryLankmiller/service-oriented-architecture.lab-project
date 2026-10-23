package ru.ifmo.se.exception;

public class InvalidHouseException extends InvalidQueryException {
    public InvalidHouseException(String field, String message) {
        super(field, message);
    }
}
