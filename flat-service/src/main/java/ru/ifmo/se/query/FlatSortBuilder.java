package ru.ifmo.se.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.micronaut.data.model.Sort.Order.Direction;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import ru.ifmo.se.model.Flat;

@Singleton
public class FlatSortBuilder {
    private static final Set<String> AVAILABLE_DIRECTIONS = Set.of("asc", "desc");

    public List<Order> buildOrder(List<String> sortQueries, Root<Flat> root, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        if (sortQueries == null || sortQueries.isEmpty()) {
            orders.add(cb.asc(root.get(SortField.ID.getName())));
            return orders;
        }

        List<SortQuery> parsed = parseSortQueries(sortQueries);

        for (SortQuery q : parsed) {
            Expression<?> expression = buildExpression(q.field, root, cb);

            orders.add(q.dir == Direction.ASC
                    ? cb.asc(expression)
                    : cb.desc(expression));
        }

        boolean hasId = parsed.stream().anyMatch(q -> q.field == SortField.ID);
        if (!hasId) {
            orders.add(cb.asc(root.get(SortField.ID.getName())));
        }

        return orders;
    }

    private Expression<?> buildExpression(SortField field, Root<Flat> root, CriteriaBuilder cb) {
        Path<?> path = buildPath(root, field);

        if (!field.getFieldClass().isEnum()) {
            return path;
        }

        var ranks = cb.<Integer>selectCase();

        for (var constanst : field.getFieldClass().getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constanst;
            ranks.when(cb.equal(path, enumConstant), enumConstant.ordinal());
        }

        return ranks.otherwise(cb.nullLiteral(Integer.class));
    }

    private <T> Path<T> buildPath(Root<Flat> root, SortField field) {
        String[] pathParts = field.getName().split("\\.");

        Path<T> path = root.get(pathParts[0]);

        for (int i = 1; i < pathParts.length; i++) {
            path = path.get(pathParts[i]);
        }

        return path;
    }

    private List<SortQuery> parseSortQueries(List<String> sortQueries) {
        List<SortQuery> parsed = new ArrayList<>();

        for (String sortQuery : sortQueries) {
            String[] queryParts = sortQuery.split("_");

            if (queryParts.length != 2) {
                continue;
            }

            String rawField = queryParts[0];
            String rawDir = queryParts[1];

            if (!SortField.isSortFieldAvailable(rawField) || !AVAILABLE_DIRECTIONS.contains(rawDir)) {
                continue;
            }

            SortField field = SortField.of(rawField);
            Direction dir = Direction.ASC;
            if (rawDir.equals("desc")) {
                dir = Direction.DESC;
            }

            parsed.add(new SortQuery(field, dir));
        }

        return parsed;
    }

    private record SortQuery(SortField field, Direction dir) {
    }

}
