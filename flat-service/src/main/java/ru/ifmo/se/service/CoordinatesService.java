package ru.ifmo.se.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.dto.CoordinatesDto;
import ru.ifmo.se.dto.CoordinatesPatchDto;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.repository.CoordinatesRepository;

@Singleton
@RequiredArgsConstructor
public class CoordinatesService {
    private final CoordinatesRepository repository;

    @Transactional
    public Coordinates createCoordinates(@Valid @NotNull CoordinatesDto dto) {
        Coordinates coordinates = Coordinates.builder()
                .x(dto.getX())
                .y(dto.getY())
                .build();

        coordinates = repository.save(coordinates);

        return coordinates;
    }

    @Transactional
    public Coordinates replaceCoordinatesById(long id, @Valid @NotNull CoordinatesDto dto) {
        Coordinates coordinates = repository.findById(id).orElseThrow();

        coordinates.setX(dto.getX());
        coordinates.setY(dto.getY());

        return coordinates;
    }

    @Transactional
    public Coordinates updateCoordinatesById(@Positive long id, @Valid @NotNull CoordinatesPatchDto dto) {
        Coordinates coordinates = repository.findById(id).orElseThrow();

        if (dto.getX() != null) {
            coordinates.setX(dto.getX());
        }
        if (dto.getY() != null) {
            coordinates.setY(dto.getY());
        }

        return coordinates;
    }
}
