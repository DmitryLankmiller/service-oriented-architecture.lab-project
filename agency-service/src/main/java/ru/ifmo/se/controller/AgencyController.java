package ru.ifmo.se.controller;

import java.math.BigDecimal;
import java.util.List;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.api.AgencyApi;
import ru.ifmo.se.client.flat.dto.FlatDto;
import ru.ifmo.se.dto.AgencyDtoMapper;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetro200ResponseInnerDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroModeParameterDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroOrderParameterDto;
import ru.ifmo.se.dto.TotalCostResponseDto;
import ru.ifmo.se.service.AgencyService;

@Controller
@Produces(MediaType.APPLICATION_XML)
@ExecuteOn(TaskExecutors.BLOCKING)
@RequiredArgsConstructor
public class AgencyController implements AgencyApi {
    private final AgencyService service;

    @Override
    public List<@Valid GetFlatsOrderedByTimeToMetro200ResponseInnerDto> getFlatsOrderedByTimeToMetro(
            @NotNull GetFlatsOrderedByTimeToMetroModeParameterDto mode,
            @NotNull GetFlatsOrderedByTimeToMetroOrderParameterDto order) {
        List<FlatDto> flats = service.listFlatsOrderedByTimeToMetro(mode, order);

        return flats.stream()
                .map(AgencyDtoMapper::toDto)
                .toList();
    }

    @Override
    public TotalCostResponseDto getTotalCost() {
        BigDecimal cost = service.calculateFlatsTotalCost();

        return new TotalCostResponseDto(cost);
    }

    @Override
    public void redirectToFlat(@NotNull @Min(1) Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'redirectToFlat'");
    }

}
