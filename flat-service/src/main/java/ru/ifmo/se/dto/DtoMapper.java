package ru.ifmo.se.dto;

import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.House;

public class DtoMapper {
    public static FlatDto toDto(Flat e) {
        FlatDto dto = new FlatDto(
                e.getId(),
                e.getName(),
                toDto(e.getCoordinates()),
                e.getCreationDate(),
                e.getArea(),
                e.getNumberOfRooms())
                .furnish(e.getFurnish() == null ? null : FurnishDto.valueOf(e.getFurnish().name()))
                .view(e.getView() == null ? null : ViewDto.valueOf(e.getView().name()))
                .transport(e.getTransport() == null ? null : TransportDto.valueOf(e.getTransport().name()))
                .house(e.getHouse() == null ? null : toDto(e.getHouse()));

        return dto;
    }

    public static CoordinatesDto toDto(Coordinates e) {
        CoordinatesDto dto = new CoordinatesDto(e.getX(), e.getY());
        return dto;
    }

    public static HouseDto toDto(House e) {
        HouseDto dto = new HouseDto(
                e.getYear(),
                e.getNumberOfFlatsOnFloor())
                .name(e.getName());
        return dto;
    }
}
