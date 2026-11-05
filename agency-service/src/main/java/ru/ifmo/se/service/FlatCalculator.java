package ru.ifmo.se.service;

import java.math.BigDecimal;

import jakarta.inject.Singleton;
import ru.ifmo.se.client.flat.dto.FlatDto;
import ru.ifmo.se.client.flat.dto.FurnishDto;
import ru.ifmo.se.client.flat.dto.HouseDto;
import ru.ifmo.se.client.flat.dto.TransportDto;
import ru.ifmo.se.client.flat.dto.ViewDto;

@Singleton
public class FlatCalculator {
    private static final BigDecimal ROOMS_MULTIPLIER = new BigDecimal("0.3");
    private static final BigDecimal COST_PER_METER = BigDecimal.valueOf(250);
    private static final BigDecimal FURNISH_COST = BigDecimal.valueOf(1000);
    private static final BigDecimal VIEW_COST = BigDecimal.valueOf(750);
    private static final BigDecimal TRANSPORT_COST = BigDecimal.valueOf(1000);
    private static final BigDecimal HOUSE_COST = BigDecimal.valueOf(3000);

    public BigDecimal calculate(FlatDto flat) {
        BigDecimal cost = BigDecimal.ZERO;
        cost = cost.add(BigDecimal.valueOf(flat.getArea()).multiply(COST_PER_METER));
        cost = cost.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(flat.getNumberOfRooms()).multiply(ROOMS_MULTIPLIER)));
        cost = cost.add(getFurnishCost(flat.getFurnish()));
        cost = cost.add(getViewCost(flat.getView()));
        cost = cost.add(getTransportCost(flat.getTransport()));
        cost = cost.add(getHouseCost(flat.getHouse()));

        return cost;
    }

    private BigDecimal getFurnishCost(FurnishDto dto) {
        if (dto == null) {
            return BigDecimal.ZERO;
        }
        double furnishMul = switch (dto) {
            case DESIGNER -> 4;
            case BAD -> -1;
            case FINE -> 2.5;
            case LITTLE -> 1;
            case NONE -> 0;
        };
        return BigDecimal.valueOf(furnishMul).multiply(FURNISH_COST);
    }

    private BigDecimal getViewCost(ViewDto dto) {
        if (dto == null) {
            return BigDecimal.ZERO;
        }
        double viewMul = switch (dto) {
            case STREET -> 2;
            case PARK -> 4;
            case NORMAL -> 1;
        };
        return BigDecimal.valueOf(viewMul).multiply(VIEW_COST);
    }

    private BigDecimal getTransportCost(TransportDto dto) {
        if (dto == null) {
            return BigDecimal.ZERO;
        }
        double transportMul = switch (dto) {
            case FEW -> 0.2;
            case LITTLE -> 0.6;
            case NORMAL -> 1;
            case ENOUGH -> 1.5;
        };
        return BigDecimal.valueOf(transportMul).multiply(TRANSPORT_COST);
    }

    private BigDecimal getHouseCost(HouseDto dto) {
        if (dto == null) {
            return HOUSE_COST;
        }
        BigDecimal cost = HOUSE_COST;
        if (dto.getYear() > 8 && dto.getYear() <= 26) {
            cost = cost.multiply(new BigDecimal("0.8"));
        } else if (dto.getYear() > 26) {
            cost = cost.multiply(new BigDecimal("0.5"));
        }
        if (dto.getNumberOfFlatsOnFloor() > 5 && dto.getNumberOfFlatsOnFloor() <= 10) {
            cost = cost.multiply(new BigDecimal("0.95"));
        } else if (dto.getNumberOfFlatsOnFloor() > 10) {
            cost = cost.multiply(new BigDecimal("0.8"));
        }

        return cost;
    }
}
