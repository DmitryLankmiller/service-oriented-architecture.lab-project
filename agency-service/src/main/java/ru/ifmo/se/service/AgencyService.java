package ru.ifmo.se.service;

import java.math.BigDecimal;
import java.util.List;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.client.flat.api.FlatsApi;
import ru.ifmo.se.client.flat.dto.FlatDto;

@Singleton
@RequiredArgsConstructor
public class AgencyService {

    private static final int REQUESTED_PAGE_SIZE = 500;

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
}
