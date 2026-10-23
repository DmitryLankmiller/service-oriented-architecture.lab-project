package ru.ifmo.se.exception;

public class MissingHouseException extends RuntimeException {
    public MissingHouseException(long flatId) {
        super("У квартиры с id=" + flatId + " отсутствует дом");
    }
}
