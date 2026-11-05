package ru.ifmo.se.service;

import static ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroModeParameterDto.FEET;
import static ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroOrderParameterDto.DESC;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.client.flat.api.FlatsApi;
import ru.ifmo.se.client.flat.dto.FlatDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroModeParameterDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroOrderParameterDto;

@Singleton
@RequiredArgsConstructor
public class AgencyService {
    private static final int REQUESTED_PAGE_SIZE = 500;

    private static final double METRO_X = 42.0;
    private static final double METRO_Y = 3.14;

    private final FlatsApi flatsApi;
    private final FlatCalculator flatCalculator;

    public BigDecimal calculateFlatsTotalCost() {
        BigDecimal sum = BigDecimal.ZERO;
        int page = 0;

        while (true) {
            List<FlatDto> flats = flatsApi.listFlats(null, null, page, REQUESTED_PAGE_SIZE);

            for (FlatDto flat : flats) {
                BigDecimal cost = flatCalculator.calculate(flat);
                sum = sum.add(cost);
            }

            if (flats.size() < REQUESTED_PAGE_SIZE) {
                break;
            }
            page++;
        }

        return sum;
    }

    public List<FlatDto> listFlatsOrderedByTimeToMetro(
            @NotNull GetFlatsOrderedByTimeToMetroModeParameterDto mode,
            @NotNull GetFlatsOrderedByTimeToMetroOrderParameterDto order) {

        List<FlatDto> flats = new ArrayList<>();
        int page = 0;

        while (true) {
            List<FlatDto> resp = flatsApi.listFlats(null, null, page, REQUESTED_PAGE_SIZE);
            flats.addAll(resp);

            if (resp.size() < REQUESTED_PAGE_SIZE) {
                break;
            }
            page++;
        }

        Comparator<FlatDto> byDist = Comparator.comparingDouble(flat -> {
            double dx = flat.getCoordinates().getX() - METRO_X;
            double dy = flat.getCoordinates().getY() - METRO_Y;

            return mode == FEET
                    ? Math.sqrt(dx * dx + dy * dy)
                    : Math.cbrt(dx + dy);
        });

        if (order == DESC) {
            byDist = byDist.reversed();
        }

        return flats.stream()
                .sorted(byDist)
                .limit(100)
                .toList();
    }
}
