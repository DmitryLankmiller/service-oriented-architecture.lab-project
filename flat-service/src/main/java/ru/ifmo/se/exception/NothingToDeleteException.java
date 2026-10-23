package ru.ifmo.se.exception;

public class NothingToDeleteException extends RuntimeException {
    public NothingToDeleteException(int numberOfRooms) {
        super("Квартиры с числом комнат " + numberOfRooms + " не найдены");
    }
}
