package ru.ifmo.se.mapper;

import org.openapitools.model.CoordinatesDto;
import org.openapitools.model.FlatDto;
import org.openapitools.model.FlatInputDto;
import org.openapitools.model.FurnishDto;
import org.openapitools.model.HouseDto;
import org.openapitools.model.TransportDto;
import org.openapitools.model.ViewDto;
import org.springframework.stereotype.Component;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.House;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;

@Component
public class FlatMapper {
    public void applyInput(FlatInputDto dto, Flat flat) {
        flat.setName(dto.getName());
        flat.setArea(dto.getArea());
        flat.setNumberOfRooms(dto.getNumberOfRooms());
        flat.setFurnish(convertEnum(dto.getFurnish(), Furnish.class));
        flat.setView(convertEnum(dto.getView(), View.class));
        flat.setTransport(convertEnum(dto.getTransport(), Transport.class));
    }

    public FlatDto toDto(Flat flat) {
        return new FlatDto()
                .id(flat.getId())
                .name(flat.getName())
                .coordinates(toDto(flat.getCoordinates()))
                .creationDate(flat.getCreationDate())
                .area(flat.getArea())
                .numberOfRooms(Math.toIntExact(flat.getNumberOfRooms()))
                .furnish(convertEnum(flat.getFurnish(), FurnishDto.class))
                .view(convertEnum(flat.getView(), ViewDto.class))
                .transport(convertEnum(flat.getTransport(), TransportDto.class))
                .house(toDto(flat.getHouse()));
    }

    private CoordinatesDto toDto(Coordinates coordinates) {
        return new CoordinatesDto(coordinates.getX(), coordinates.getY());
    }

    private HouseDto toDto(House house) {
        if (house == null) {
            return null;
        }

        return new HouseDto(house.getYear(), house.getNumberOfFlatsOnFloor())
                .name(house.getName());
    }

    private <T extends Enum<T>> T convertEnum(Enum<?> value, Class<T> targetType) {
        return value == null ? null : Enum.valueOf(targetType, value.name());
    }
}
