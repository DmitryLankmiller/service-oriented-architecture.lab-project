package ru.ifmo.se.controller;

import ru.ifmo.se.api.FlatsApi;
import ru.ifmo.se.dto.CountFlatsByHouseHouseParameterDto;
import ru.ifmo.se.dto.CountResponseDto;
import ru.ifmo.se.dto.FlatDto;
import ru.ifmo.se.dto.FlatInputDto;
import ru.ifmo.se.dto.FlatPatchDto;
import ru.ifmo.se.dto.ViewDto;

import java.util.List;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Controller
public class FlatsController implements FlatsApi {

    @Override
    public CountResponseDto countFlatsByHouse(@Nullable(inherited = true) CountFlatsByHouseHouseParameterDto house,
            @Nullable(inherited = true) String houseName, @Nullable(inherited = true) @Min(1) @Max(148) Long houseYear,
            @Nullable(inherited = true) @Min(1) Integer houseNumberOfFlatsOnFloor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countFlatsByHouse'");
    }

    @Override
    public HttpResponse<@Valid FlatDto> createFlat(@NotNull @Valid FlatInputDto flatInputDto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createFlat'");
    }

    @Override
    public HttpResponse<Void> deleteFlat(@NotNull @Min(1) Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteFlat'");
    }

    @Override
    public FlatDto deleteOneFlatByNumberOfRooms(@NotNull @Min(1) @Max(20) Integer numberOfRooms) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneFlatByNumberOfRooms'");
    }

    @Override
    public FlatDto getFlat(@NotNull @Min(1) Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlat'");
    }

    @Override
    public List<@Valid FlatDto> getFlatsWithGreaterView(@NotNull ViewDto view) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlatsWithGreaterView'");
    }

    @Override
    public List<@Valid FlatDto> listFlats(@Nullable(inherited = true) @Size(min = 1) List<@NotNull String> filter,
            @Nullable(inherited = true) @Size(min = 1) List<@NotNull String> sort,
            @Nullable(inherited = true) @Min(0) Integer page, @Nullable(inherited = true) @Min(1) Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listFlats'");
    }

    @Override
    public FlatDto patchFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatPatchDto flatPatchDto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'patchFlat'");
    }

    @Override
    public FlatDto replaceFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatInputDto flatInputDto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replaceFlat'");
    }

}
