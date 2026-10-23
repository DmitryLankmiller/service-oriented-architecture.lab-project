package ru.ifmo.se.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.FlatsApi;
import org.openapitools.model.CountResponseDto;
import org.openapitools.model.FlatDto;
import org.openapitools.model.FlatInputDto;
import org.openapitools.model.FlatPatchDto;
import org.openapitools.model.ViewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ifmo.se.service.FlatService;

@RestController
@RequiredArgsConstructor
public class FlatsController implements FlatsApi {
    private final FlatService flatService;

    @Override
    public ResponseEntity<CountResponseDto> countFlatsByHouse(@Valid String house, @Valid String houseName,
            @Min(1) @Max(148) @Valid Long houseYear, @Min(1) @Valid Integer houseNumberOfFlatsOnFloor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countFlatsByHouse'");
    }

    @Override
    public ResponseEntity<FlatDto> createFlat(@Valid FlatInputDto flatInput) {
        FlatDto flat = flatService.createFlat(flatInput);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(flat.getId())
                .toUri();
        return ResponseEntity.created(uri)
                .body(flat);
    }

    @Override
    public ResponseEntity<Void> deleteFlat(@Min(1) Long id) {
        flatService.deleteFlatById(id);
        return ResponseEntity.noContent()
                .build();
    }

    @Override
    public ResponseEntity<FlatDto> deleteOneFlatByNumberOfRooms(
            @NotNull @Min(1) @Max(20) @Valid Integer numberOfRooms) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneFlatByNumberOfRooms'");
    }

    @Override
    public ResponseEntity<FlatDto> getFlat(@Min(1) Long id) {
        return ResponseEntity.ok(flatService.getFlatById(id));
    }

    @Override
    public ResponseEntity<List<FlatDto>> getFlatsWithGreaterView(ViewDto view) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlatsWithGreaterView'");
    }

    @Override
    public ResponseEntity<List<FlatDto>> listFlats(@Size(min = 1) List<String> filter, @Size(min = 1) List<String> sort,
            @Min(0) @Valid Integer page, @Min(1) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listFlats'");
    }

    @Override
    public ResponseEntity<FlatDto> patchFlat(@Min(1) Long id, @Valid FlatPatchDto flatPatch) {
        return ResponseEntity.ok(flatService.updateFlatById(id, flatPatch));
    }

    @Override
    public ResponseEntity<FlatDto> replaceFlat(@Min(1) Long id, @Valid FlatInputDto flatInput) {
        return ResponseEntity.ok(flatService.replaceFlatById(id, flatInput));
    }

}
