package ru.ifmo.se.query;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;
import static ru.ifmo.se.query.FilterOperator.*;

public enum FilterField {
    ID("id", Long.class, false, COMPARISON_OPERATORS),
    NAME("name", String.class, false, STRING_OPERATORS),
    COORDINATES_X("coordinates.x", Float.class, false, COMPARISON_OPERATORS),
    COORDINATES_Y("coordinates.y", Integer.class, false, COMPARISON_OPERATORS),
    CREATION_DATE("creationDate", LocalDate.class, false, COMPARISON_OPERATORS),
    AREA("area", Integer.class, false, COMPARISON_OPERATORS),
    NUMBER_OF_ROOMS("numberOfRooms", Integer.class, false, COMPARISON_OPERATORS),
    FURNISH("furnish", Furnish.class, true, COMPARISON_OPERATORS),
    VIEW("view", View.class, true, COMPARISON_OPERATORS),
    TRANSPORT("transport", Transport.class, true, COMPARISON_OPERATORS),
    HOUSE_NAME("house.name", String.class, true, STRING_OPERATORS),
    HOUSE_YEAR("house.year", Long.class, false, COMPARISON_OPERATORS),
    HOUSE_NUMBER_OF_FLATS_ON_FLOOR("house.numberOfFlatsOnFloor", Integer.class, false, COMPARISON_OPERATORS);

    private static final Set<String> AVAILABLE_FILTER_FIELDS = Stream.of(FilterField.values()).map(f -> f.getName())
            .collect(Collectors.toSet());

    private final String field;
    private final Class<?> fieldClass;
    private final boolean nullable;
    private final Set<FilterOperator> availableOperators;

    FilterField(String field, Class<?> fieldClass, boolean nullable, Set<FilterOperator> availableOperators) {
        this.field = field;
        this.fieldClass = fieldClass;
        this.nullable = nullable;
        this.availableOperators = availableOperators;
    }

    public static FilterField of(String field) {
        for (var f : FilterField.values()) {
            if (f.field.equals(field)) {
                return f;
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean isFilterFieldAvailable(String field) {
        return AVAILABLE_FILTER_FIELDS.contains(field);
    }

    public boolean isOperatorAvailable(FilterOperator op) {
        return availableOperators.contains(op);
    }

    public String getName() {
        return field;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public boolean isNullable() {
        return nullable;
    }
}
