package ru.ifmo.se.exception;

public class BadHouseException extends RuntimeException {

    public BadHouseException() {
        super("provided flat does not have house");
    }
}
