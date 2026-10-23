package ru.ifmo.se.exception;

public class FlatNotFoundException extends RuntimeException {
    public FlatNotFoundException(long id) {
        super("Квартира с id=" + id + " не найдена");
    }
}
