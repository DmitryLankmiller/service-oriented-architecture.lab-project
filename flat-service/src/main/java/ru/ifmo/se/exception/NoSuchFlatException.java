package ru.ifmo.se.exception;

public class NoSuchFlatException extends RuntimeException {
    private static final String NO_FLAT_BY_ID_TEMPLATE = "flat with id=%d does not exist";

    public NoSuchFlatException(long id) {
        super(NO_FLAT_BY_ID_TEMPLATE.formatted(id));
    }
}
