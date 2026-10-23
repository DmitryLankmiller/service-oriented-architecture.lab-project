package ru.ifmo.se.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.FlatDto;
import org.openapitools.model.FlatInputDto;
import org.openapitools.model.FlatPatchDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.ifmo.se.exception.FlatNotFoundException;
import ru.ifmo.se.exception.MissingHouseException;
import ru.ifmo.se.mapper.FlatMapper;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.House;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;
import ru.ifmo.se.repository.FlatRepository;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlatService {
    private final FlatRepository flatRepository;
    private final CoordinatesService coordinatesService;
    private final HouseService houseService;
    private final FlatMapper flatMapper;

    @Transactional
    public FlatDto createFlat(@NotNull @Valid FlatInputDto dto) {
        Flat flat = new Flat();
        flatMapper.applyInput(dto, flat);

        flat.setCoordinates(coordinatesService.createCoordinates(dto.getCoordinates()));

        if (dto.getHouse() != null) {
            flat.setHouse(houseService.createHouse(dto.getHouse()));
        }

        return flatMapper.toDto(flatRepository.saveAndFlush(flat));
    }

    public FlatDto getFlatById(@NotNull @Positive Long id) {
        return flatMapper.toDto(findFlat(id));
    }

    @Transactional
    public void deleteFlatById(@NotNull @Positive Long id) {
        Flat flat = findFlat(id);
        Coordinates coordinates = flat.getCoordinates();
        House house = flat.getHouse();

        flatRepository.delete(flat);
        flatRepository.flush();
        coordinatesService.deleteCoordinates(coordinates);
        deleteHouseIfUnused(house);
    }

    @Transactional
    public FlatDto replaceFlatById(
            @NotNull @Positive Long id,
            @NotNull @Valid FlatInputDto dto) {
        Flat flat = findFlat(id);
        House previousHouse = flat.getHouse();

        flatMapper.applyInput(dto, flat);

        coordinatesService.replaceCoordinates(flat.getCoordinates(), dto.getCoordinates());

        if (dto.getHouse() == null) {
            flat.setHouse(null);
        } else if (previousHouse == null) {
            flat.setHouse(houseService.createHouse(dto.getHouse()));
        } else {
            houseService.replaceHouse(previousHouse, dto.getHouse());
        }

        flatRepository.flush();

        if (dto.getHouse() == null) {
            deleteHouseIfUnused(previousHouse);
        }

        return flatMapper.toDto(flat);
    }

    @Transactional
    public FlatDto updateFlatById(
            @NotNull @Positive Long id,
            @NotNull @Valid FlatPatchDto dto) {
        Flat flat = findFlat(id);

        if (dto.getName() != null) {
            flat.setName(dto.getName());
        }
        if (dto.getCoordinates() != null) {
            coordinatesService.updateCoordinates(flat.getCoordinates(), dto.getCoordinates());
        }
        if (dto.getArea() != null) {
            flat.setArea(dto.getArea());
        }
        if (dto.getNumberOfRooms() != null) {
            flat.setNumberOfRooms(dto.getNumberOfRooms());
        }
        if (dto.getFurnish() != null) {
            flat.setFurnish(Furnish.valueOf(dto.getFurnish().name()));
        }
        if (dto.getView() != null) {
            flat.setView(View.valueOf(dto.getView().name()));
        }
        if (dto.getTransport() != null) {
            flat.setTransport(Transport.valueOf(dto.getTransport().name()));
        }
        if (dto.getHouse() != null) {
            if (flat.getHouse() == null) {
                throw new MissingHouseException(id);
            }
            houseService.updateHouse(flat.getHouse(), dto.getHouse());
        }

        flatRepository.flush();

        return flatMapper.toDto(flat);
    }

    private Flat findFlat(long id) {
        return flatRepository.findById(id).orElseThrow(() -> new FlatNotFoundException(id));
    }

    private void deleteHouseIfUnused(House house) {
        if (house != null && !flatRepository.existsByHouseId(house.getId())) {
            houseService.deleteHouse(house);
        }
    }
}
