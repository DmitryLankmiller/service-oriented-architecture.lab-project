package ru.ifmo.se.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.CoordinatesDto;
import org.openapitools.model.CoordinatesPatchDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.repository.CoordinatesRepository;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;

    public Coordinates createCoordinates(@NotNull @Valid CoordinatesDto dto) {
        return coordinatesRepository.save(
                Coordinates.builder()
                        .x(dto.getX())
                        .y(dto.getY())
                        .build());
    }

    public void replaceCoordinates(
            @NotNull Coordinates coordinates,
            @NotNull @Valid CoordinatesDto dto) {
        coordinates.setX(dto.getX());
        coordinates.setY(dto.getY());
    }

    public void updateCoordinates(
            @NotNull Coordinates coordinates,
            @NotNull @Valid CoordinatesPatchDto dto) {
        if (dto.getX() != null) {
            coordinates.setX(dto.getX());
        }
        if (dto.getY() != null) {
            coordinates.setY(dto.getY());
        }
    }

    public void deleteCoordinates(@NotNull Coordinates coordinates) {
        coordinatesRepository.delete(coordinates);
    }
}
