package ru.ifmo.se.query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import ru.ifmo.se.exception.InvalidQueryException;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.House;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;

enum FlatField {
    ID("id", Long.class, false),
    NAME("name", String.class, false),
    COORDINATES_X("coordinates.x", Float.class, false),
    COORDINATES_Y("coordinates.y", Integer.class, false),
    CREATION_DATE("creationDate", LocalDate.class, false),
    AREA("area", Integer.class, false),
    NUMBER_OF_ROOMS("numberOfRooms", Long.class, false),
    FURNISH("furnish", Furnish.class, true),
    VIEW("view", View.class, true),
    TRANSPORT("transport", Transport.class, true),
    HOUSE("house", House.class, true),
    HOUSE_NAME("house.name", String.class, true),
    HOUSE_YEAR("house.year", Long.class, true),
    HOUSE_NUMBER_OF_FLATS_ON_FLOOR("house.numberOfFlatsOnFloor", Integer.class, true);

    private final String property;
    private final Class<?> type;
    private final boolean nullable;

    FlatField(String property, Class<?> type, boolean nullable) {
        this.property = property;
        this.type = type;
        this.nullable = nullable;
    }

    static FlatField require(String property) {
        return Arrays.stream(values())
                .filter(field -> field.property.equals(property))
                .findFirst()
                .orElseThrow(() -> new InvalidQueryException(property, "Неизвестное поле: " + property));
    }

    String property() {
        return property;
    }

    boolean nullable() {
        return nullable;
    }

    boolean isString() {
        return type == String.class;
    }

    boolean isEnum() {
        return type.isEnum();
    }

    boolean isAssociation() {
        return this == HOUSE;
    }

    Path<?> path(Root<Flat> root) {
        int separator = property.indexOf('.');
        if (separator < 0) {
            return root.get(property);
        }
        String association = property.substring(0, separator);
        Join<?, ?> join = root.getJoins().stream()
                .filter(item -> item.getAttribute().getName().equals(association)
                        && item.getJoinType() == JoinType.LEFT)
                .findFirst()
                .orElseGet(() -> root.join(association, JoinType.LEFT));
        return join.get(property.substring(separator + 1));
    }

    Object parse(String value) {
        try {
            if (type == String.class) {
                return value;
            }
            if (type == Integer.class) {
                return Integer.valueOf(value);
            }
            if (type == Long.class) {
                return Long.valueOf(value);
            }
            if (type == Float.class) {
                float number = Float.parseFloat(value);
                if (!Float.isFinite(number)) {
                    throw new NumberFormatException();
                }
                return number;
            }
            if (type == LocalDate.class) {
                return LocalDate.parse(value);
            }
            if (isEnum()) {
                return Arrays.stream(type.getEnumConstants())
                        .filter(item -> ((Enum<?>) item).name().equals(value))
                        .findFirst().orElseThrow(IllegalArgumentException::new);
            }
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw new InvalidQueryException(property, "Некорректное значение поля " + property + ": " + value);
        }
        throw new InvalidQueryException(property, "Для house поддерживаются только eq_null и neq_null");
    }

    Expression<Integer> enumOrder(CriteriaBuilder builder, Expression<?> expression) {
        CriteriaBuilder.Case<Integer> ordering = builder.selectCase();
        Object[] constants = type.getEnumConstants();
        for (Object constant : constants) {
            ordering.when(builder.equal(expression, constant), ((Enum<?>) constant).ordinal());
        }
        return ordering.otherwise(constants.length);
    }
}
