package ru.ifmo.se.controller;

import java.util.List;

import org.openapitools.api.FlatsApi;
import org.openapitools.model.CountResponse;
import org.openapitools.model.Flat;
import org.openapitools.model.FlatInput;
import org.openapitools.model.FlatPatch;
import org.openapitools.model.View;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
public class FlatsController implements FlatsApi {

    @Override
    public ResponseEntity<CountResponse> countFlatsByHouse(@Valid String house, @Valid String houseName,
            @Min(1) @Max(148) @Valid Long houseYear, @Min(1) @Valid Integer houseNumberOfFlatsOnFloor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countFlatsByHouse'");
    }

    @Override
    public ResponseEntity<Flat> createFlat(@Valid FlatInput flatInput) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createFlat'");
    }

    @Override
    public ResponseEntity<Void> deleteFlat(@Min(1) Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteFlat'");
    }

    @Override
    public ResponseEntity<Flat> deleteOneFlatByNumberOfRooms(@NotNull @Min(1) @Max(20) @Valid Integer numberOfRooms) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneFlatByNumberOfRooms'");
    }

    @Override
    public ResponseEntity<Flat> getFlat(@Min(1) Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlat'");
    }

    @Override
    public ResponseEntity<List<Flat>> getFlatsWithGreaterView(View view) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFlatsWithGreaterView'");
    }

    @Override
    public ResponseEntity<List<Flat>> listFlats(@Size(min = 1) List<String> filter, @Size(min = 1) List<String> sort,
            @Min(0) @Valid Integer page, @Min(1) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listFlats'");
    }

    @Override
    public ResponseEntity<Flat> patchFlat(@Min(1) Long id, @Valid FlatPatch flatPatch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'patchFlat'");
    }

    @Override
    public ResponseEntity<Flat> replaceFlat(@Min(1) Long id, @Valid FlatInput flatInput) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replaceFlat'");
    }

}
