package ru.ifmo.se.query;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.ifmo.se.model.Furnish;
import ru.ifmo.se.model.Transport;
import ru.ifmo.se.model.View;

public enum SortField {
    ID("id", Long.class),
    NAME("name", String.class),
    COORDINATES_X("coordinates.x", Float.class),
    COORDINATES_Y("coordinates.y", Integer.class),
    CREATION_DATE("creationDate", LocalDate.class),
    AREA("area", Integer.class),
    NUMBER_OF_ROOMS("numberOfRooms", Integer.class),
    FURNISH("furnish", Furnish.class),
    VIEW("view", View.class),
    TRANSPORT("transport", Transport.class),
    HOUSE_NAME("house.name", String.class),
    HOUSE_YEAR("house.year", Long.class),
    HOUSE_NUMBER_OF_FLATS_ON_FLOOR("house.numberOfFlatsOnFloor", Integer.class);

    private static final Set<String> AVAILABLE_SORT_FIELDS = Stream.of(SortField.values()).map(f -> f.getName())
            .collect(Collectors.toSet());

    private final String field;
    private final Class<?> fieldClass;

    SortField(String field, Class<?> fieldClass) {
        this.field = field;
        this.fieldClass = fieldClass;
    }

    public static SortField of(String field) {
        for (var f : SortField.values()) {
            if (f.field.equals(field)) {
                return f;
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean isSortFieldAvailable(String field) {
        return AVAILABLE_SORT_FIELDS.contains(field);
    }

    public String getName() {
        return field;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }
}
