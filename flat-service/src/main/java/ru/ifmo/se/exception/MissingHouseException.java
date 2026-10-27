package ru.ifmo.se.exception;

public class MissingHouseException extends RuntimeException {

    public MissingHouseException() {
        super("provided flat does not have house");
    }
}
