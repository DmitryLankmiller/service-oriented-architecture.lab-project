package ru.ifmo.se.exception;

public class NoFlatToDeleteException extends RuntimeException {

    public NoFlatToDeleteException() {
        super("no flat to delete");
    }
}
