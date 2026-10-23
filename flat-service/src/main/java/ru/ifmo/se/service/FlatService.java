package ru.ifmo.se.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.CountResponseDto;
import org.openapitools.model.FlatDto;
import org.openapitools.model.FlatInputDto;
import org.openapitools.model.FlatPatchDto;
import org.openapitools.model.ViewDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.ifmo.se.exception.FlatNotFoundException;
import ru.ifmo.se.exception.InvalidHouseException;
import ru.ifmo.se.exception.InvalidQueryException;
import ru.ifmo.se.exception.MissingHouseException;
import ru.ifmo.se.exception.NothingToDeleteException;
import ru.ifmo.se.mapper.FlatMapper;
import ru.ifmo.se.model.Coordinates;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.House;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;
import ru.ifmo.se.query.FlatFilterBuilder;
import ru.ifmo.se.query.FlatSortBuilder;
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
    private final FlatFilterBuilder flatFilterBuilder;
    private final FlatSortBuilder flatSortBuilder;

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

    public List<FlatDto> listFlats(
            @Size(min = 1) List<String> filters,
            @Size(min = 1) List<String> sorting,
            @Min(0) Integer page,
            @Min(1) Integer size) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;
        if ((long) pageNumber * pageSize > Integer.MAX_VALUE) {
            throw new InvalidQueryException("page", "Смещение страницы слишком большое");
        }
        var specification = flatFilterBuilder.build(filters).and(flatSortBuilder.build(sorting));

        return flatRepository.findAll(specification, PageRequest.of(pageNumber, pageSize))
                .getContent().stream().map(flatMapper::toDto).toList();
    }

    @Transactional
    public void deleteFlatById(@NotNull @Positive Long id) {
        deleteFlat(findFlat(id));
    }

    @Transactional
    public FlatDto deleteOneFlatByNumberOfRooms(@NotNull @Min(1) @Max(20) Integer numberOfRooms) {
        Flat flat = flatRepository.findFirstByNumberOfRoomsOrderByIdAsc(numberOfRooms)
                .orElseThrow(() -> new NothingToDeleteException(numberOfRooms));
        FlatDto deletedFlat = flatMapper.toDto(flat);

        deleteFlat(flat);

        return deletedFlat;
    }

    public CountResponseDto countFlatsByHouse(
            String house,
            String houseName,
            @Min(1) @Max(148) Long houseYear,
            @Min(1) Integer houseNumberOfFlatsOnFloor) {
        if (house != null) {
            if (!house.equals("null")) {
                throw new InvalidHouseException("house", "Параметр house допускает только значение null");
            }
            if (houseName != null || houseYear != null || houseNumberOfFlatsOnFloor != null) {
                throw new InvalidHouseException("house", "Параметр house=null несовместим с полями дома");
            }
            return new CountResponseDto(flatRepository.countByHouseIsNull());
        }

        if (houseYear == null) {
            throw new InvalidHouseException("house.year", "Укажите house.year или house=null");
        }

        if (houseNumberOfFlatsOnFloor == null) {
            throw new InvalidHouseException("house.numberOfFlatsOnFloor", "Укажите house.numberOfFlatsOnFloor");
        }

        long count = flatRepository.countByHouse_NameAndHouse_YearAndHouse_NumberOfFlatsOnFloor(
                houseName, houseYear, houseNumberOfFlatsOnFloor);

        return new CountResponseDto(count);
    }

    public List<FlatDto> getFlatsWithGreaterView(@NotNull ViewDto view) {
        View threshold = View.valueOf(view.name());
        List<View> greaterViews = Arrays.stream(View.values())
                .filter(value -> value.ordinal() > threshold.ordinal()).toList();
        Specification<Flat> specification = (root, query, builder) -> greaterViews.isEmpty()
                ? builder.disjunction()
                : root.get("view").in(greaterViews);
        return flatRepository.findAll(specification, Sort.by("id"))
                .stream().map(flatMapper::toDto).toList();
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

    private void deleteFlat(Flat flat) {
        Coordinates coordinates = flat.getCoordinates();
        House house = flat.getHouse();
        flatRepository.delete(flat);
        flatRepository.flush();
        coordinatesService.deleteCoordinates(coordinates);
        deleteHouseIfUnused(house);
    }

    private void deleteHouseIfUnused(House house) {
        if (house != null && !flatRepository.existsByHouseId(house.getId())) {
            houseService.deleteHouse(house);
        }
    }
}
