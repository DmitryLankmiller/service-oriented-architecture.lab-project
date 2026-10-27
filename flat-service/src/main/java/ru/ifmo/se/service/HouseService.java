package ru.ifmo.se.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.dto.HouseDto;
import ru.ifmo.se.dto.HousePatchDto;
import ru.ifmo.se.model.House;
import ru.ifmo.se.repository.HouseRepository;

@Singleton
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository repository;

    @Transactional
    public House createHouse(@Valid @NotNull HouseDto dto) {
        House house = House.builder()
                .name(dto.getName())
                .year(dto.getYear())
                .numberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor())
                .build();

        house = repository.save(house);

        return house;
    }

    @Transactional
    public House replaceHouseById(long id, @Valid @NotNull HouseDto dto) {
        House house = repository.findById(id).orElseThrow();

        house.setName(dto.getName());
        house.setYear(dto.getYear());
        house.setNumberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor());

        return house;
    }

    @Transactional
    public House updateHouseById(@Positive long id, @Valid @NotNull HousePatchDto dto) {
        House house = repository.findById(id).orElseThrow();

        if (dto.getName() != null) {
            house.setName(dto.getName());
        }
        if (dto.getYear() != null) {
            house.setYear(dto.getYear());
        }
        if (dto.getNumberOfFlatsOnFloor() != null) {
            house.setNumberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor());
        }

        return house;
    }
}
