package ru.ifmo.se.service;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.dto.FlatInputDto;
import ru.ifmo.se.dto.FlatPatchDto;
import ru.ifmo.se.exception.MissingHouseException;
import ru.ifmo.se.exception.NoFlatToDeleteException;
import ru.ifmo.se.exception.NoSuchFlatException;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.House;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;
import ru.ifmo.se.repository.FlatRepository;

@Singleton
@RequiredArgsConstructor
public class FlatService {
    private final FlatRepository repository;
    private final CoordinatesService coordinatesService;
    private final HouseService houseService;

    @Transactional
    public Flat createFlat(@Valid @NotNull FlatInputDto dto) {
        Coordinates coordinates = coordinatesService.createCoordinates(dto.getCoordinates());

        House house = null;
        if (dto.getHouse() != null) {
            house = houseService.createHouse(dto.getHouse());
        }

        Flat flat = Flat.builder()
                .name(dto.getName())
                .coordinates(coordinates)
                .area(dto.getArea())
                .numberOfRooms(dto.getNumberOfRooms())
                .furnish(dto.getFurnish() == null ? null : Furnish.valueOf(dto.getFurnish().getValue()))
                .view(dto.getView() == null ? null : View.valueOf(dto.getView().getValue()))
                .transport(dto.getTransport() == null ? null : Transport.valueOf(dto.getTransport().getValue()))
                .house(house)
                .build();

        flat = repository.save(flat);

        return flat;
    }

    private Flat findFlatById(long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchFlatException(id));
    }

    @Transactional
    public Flat getFlatById(long id) {
        return findFlatById(id);
    }

    @Transactional
    public Flat replaceFlatById(@Positive long id, @Valid @NotNull FlatInputDto dto) {
        Flat flat = findFlatById(id);

        coordinatesService.replaceCoordinatesById(flat.getCoordinates().getId(), dto.getCoordinates());

        if (flat.getHouse() == null && dto.getHouse() != null) {
            House house = houseService.createHouse(dto.getHouse());
            flat.setHouse(house);
        } else {
            if (dto.getHouse() == null) {
                flat.setHouse(null);
            } else {
                houseService.replaceHouseById(flat.getHouse().getId(), dto.getHouse());
            }
        }

        flat.setName(dto.getName());
        flat.setArea(dto.getArea());
        flat.setNumberOfRooms(dto.getNumberOfRooms());
        flat.setFurnish(dto.getFurnish() == null ? null : Furnish.valueOf(dto.getFurnish().getValue()));
        flat.setView(dto.getView() == null ? null : View.valueOf(dto.getView().getValue()));
        flat.setTransport(dto.getTransport() == null ? null : Transport.valueOf(dto.getTransport().getValue()));

        return flat;
    }

    @Transactional
    public Flat updateFlatById(@Positive long id, @Valid @NotNull FlatPatchDto dto) {
        Flat flat = findFlatById(id);

        if (dto.getCoordinates() != null) {
            coordinatesService.updateCoordinatesById(flat.getCoordinates().getId(), dto.getCoordinates());
        }

        if (dto.getHouse() != null) {
            if (flat.getHouse() == null) {
                throw new MissingHouseException();
            }
            houseService.updateHouseById(flat.getHouse().getId(), dto.getHouse());
        }

        if (dto.getName() != null) {
            flat.setName(dto.getName());
        }
        if (dto.getArea() != null) {
            flat.setArea(dto.getArea());
        }
        if (dto.getNumberOfRooms() != null) {
            flat.setNumberOfRooms(dto.getNumberOfRooms());
        }
        if (dto.getFurnish() != null) {
            flat.setFurnish(Furnish.valueOf(dto.getFurnish().getValue()));
        }
        if (dto.getView() != null) {
            flat.setView(View.valueOf(dto.getView().getValue()));
        }
        if (dto.getTransport() != null) {
            flat.setTransport(Transport.valueOf(dto.getTransport().getValue()));
        }

        return flat;
    }

    @Transactional
    public void deleteFlatById(@Positive long id) {
        Flat flat = repository.findById(id).orElseThrow(() -> new NoFlatToDeleteException());
        repository.delete(flat);
    }

}
