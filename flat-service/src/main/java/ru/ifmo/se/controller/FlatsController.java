package ru.ifmo.se.controller;

import ru.ifmo.se.api.FlatsApi;
import ru.ifmo.se.dto.CountFlatsByHouseHouseParameterDto;
import ru.ifmo.se.dto.CountResponseDto;
import ru.ifmo.se.dto.DtoMapper;

import static ru.ifmo.se.dto.DtoMapper.toDto;
import ru.ifmo.se.dto.FlatDto;
import ru.ifmo.se.dto.FlatInputDto;
import ru.ifmo.se.dto.FlatPatchDto;
import ru.ifmo.se.dto.ViewDto;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.service.FlatService;

import java.net.URI;
import java.util.List;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Controller
@ExecuteOn(TaskExecutors.BLOCKING)
@RequiredArgsConstructor
public class FlatsController implements FlatsApi {
    private final FlatService service;
    private final ServerContextPathProvider contextPathProvider;

    @Override
    public CountResponseDto countFlatsByHouse(@Nullable(inherited = true) CountFlatsByHouseHouseParameterDto house,
            @Nullable(inherited = true) String houseName, @Nullable(inherited = true) @Min(1) @Max(148) Long houseYear,
            @Nullable(inherited = true) @Min(1) Integer houseNumberOfFlatsOnFloor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countFlatsByHouse'");
    }

    @Override
    public HttpResponse<@Valid FlatDto> createFlat(@NotNull @Valid FlatInputDto flatInputDto) {
        Flat flat = service.createFlat(flatInputDto);

        URI uri = UriBuilder.of("/")
                .path(contextPathProvider.getContextPath())
                .path("/flats")
                .path(Long.toString(flat.getId()))
                .build();

        return HttpResponse.created(uri)
                .body(toDto(flat));
    }

    @Override
    public HttpResponse<Void> deleteFlat(@NotNull @Min(1) Long id) {
        service.deleteFlatById(id);

        return HttpResponse.noContent();
    }

    @Override
    public FlatDto deleteOneFlatByNumberOfRooms(@NotNull @Min(1) @Max(20) Integer numberOfRooms) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneFlatByNumberOfRooms'");
    }

    @Override
    public FlatDto getFlat(@NotNull @Min(1) Long id) {
        Flat flat = service.getFlatById(id);

        return toDto(flat);
    }

    @Override
    public List<@Valid FlatDto> getFlatsWithGreaterView(@NotNull ViewDto view) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlatsWithGreaterView'");
    }

    @Override
    public List<@Valid FlatDto> listFlats(@Nullable(inherited = true) List<@NotNull String> filter,
            @Nullable(inherited = true) @Size(min = 1) List<@NotNull String> sort,
            @Nullable(inherited = true) @Min(0) Integer page, @Nullable(inherited = true) @Min(1) Integer size) {
        List<Flat> flatsPage = service.getFlats(filter, sort, page, size);

        return flatsPage.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public FlatDto patchFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatPatchDto flatPatchDto) {
        Flat flat = service.updateFlatById(id, flatPatchDto);

        return toDto(flat);
    }

    @Override
    public FlatDto replaceFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatInputDto flatInputDto) {
        Flat flat = service.replaceFlatById(id, flatInputDto);

        return toDto(flat);
    }

}
