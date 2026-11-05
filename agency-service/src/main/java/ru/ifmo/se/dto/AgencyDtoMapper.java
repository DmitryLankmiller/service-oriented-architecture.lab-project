package ru.ifmo.se.dto;

import ru.ifmo.se.client.flat.dto.CoordinatesDto;
import ru.ifmo.se.client.flat.dto.FlatDto;
import ru.ifmo.se.client.flat.dto.FurnishDto;
import ru.ifmo.se.client.flat.dto.HouseDto;
import ru.ifmo.se.client.flat.dto.TransportDto;
import ru.ifmo.se.client.flat.dto.ViewDto;

public final class AgencyDtoMapper {
    private AgencyDtoMapper() {
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerDto toDto(FlatDto source) {
        if (source == null) {
            return null;
        }
        return new GetFlatsOrderedByTimeToMetro200ResponseInnerDto(
                source.getId(),
                source.getName(),
                toDto(source.getCoordinates()),
                source.getCreationDate(),
                source.getArea(),
                source.getNumberOfRooms())
                .furnish(toDto(source.getFurnish()))
                .view(toDto(source.getView()))
                .transport(toDto(source.getTransport()))
                .house(toDto(source.getHouse()));
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerCoordinatesDto toDto(CoordinatesDto source) {
        if (source == null) {
            return null;
        }
        return new GetFlatsOrderedByTimeToMetro200ResponseInnerCoordinatesDto(source.getX(), source.getY());
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerHouseDto toDto(HouseDto source) {
        if (source == null) {
            return null;
        }
        return new GetFlatsOrderedByTimeToMetro200ResponseInnerHouseDto(
                source.getYear(), source.getNumberOfFlatsOnFloor())
                .name(source.getName());
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerFurnishDto toDto(FurnishDto source) {
        return source == null ? null
                : GetFlatsOrderedByTimeToMetro200ResponseInnerFurnishDto.fromValue(source.getValue());
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerViewDto toDto(ViewDto source) {
        return source == null ? null
                : GetFlatsOrderedByTimeToMetro200ResponseInnerViewDto.fromValue(source.getValue());
    }

    public static GetFlatsOrderedByTimeToMetro200ResponseInnerTransportDto toDto(TransportDto source) {
        return source == null ? null
                : GetFlatsOrderedByTimeToMetro200ResponseInnerTransportDto.fromValue(source.getValue());
    }
}
