package ru.ifmo.se.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.HouseDto;
import org.openapitools.model.HousePatchDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.ifmo.se.model.House;
import ru.ifmo.se.repository.HouseRepository;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class HouseService {
    private final HouseRepository houseRepository;

    public House createHouse(@NotNull @Valid HouseDto dto) {
        return houseRepository.save(House.builder()
                .name(dto.getName())
                .year(dto.getYear())
                .numberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor())
                .build());
    }

    public void replaceHouse(@NotNull House house, @NotNull @Valid HouseDto dto) {
        house.setName(dto.getName());
        house.setYear(dto.getYear());
        house.setNumberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor());
    }

    public void updateHouse(@NotNull House house, @NotNull @Valid HousePatchDto dto) {
        if (dto.getName() != null) {
            house.setName(dto.getName());
        }
        if (dto.getYear() != null) {
            house.setYear(dto.getYear());
        }
        if (dto.getNumberOfFlatsOnFloor() != null) {
            house.setNumberOfFlatsOnFloor(dto.getNumberOfFlatsOnFloor());
        }
    }

    public void deleteHouse(@NotNull House house) {
        houseRepository.delete(house);
    }
}
