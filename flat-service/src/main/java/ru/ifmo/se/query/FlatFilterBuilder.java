package ru.ifmo.se.query;

import static ru.ifmo.se.query.FilterOperator.EQ;
import static ru.ifmo.se.query.FilterOperator.NEQ;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.model.Flat;

@Singleton
@RequiredArgsConstructor
public class FlatFilterBuilder {
    private final ConversionService conversionService;

    public PredicateSpecification<Flat> buildSpec(List<@NotNull String> filterQueries) {
        if (filterQueries == null || filterQueries.isEmpty()) {
            return (root, cb) -> cb.conjunction();
        }

        List<FilterQuery> parsedQueries = parseFilterQuery(filterQueries);

        PredicateSpecification<Flat> spec = (root, cb) -> cb.conjunction();

        for (FilterQuery q : parsedQueries) {
            switch (q.operator) {
                case EQ:
                    if (q.value == null) {
                        spec = spec.and((root, cb) -> cb.isNull(buildPath(root, q)));
                        break;
                    }
                    spec = spec.and((root, cb) -> cb.equal(buildPath(root, q), q.value));
                    break;
                case NEQ:
                    if (q.value == null) {
                        spec = spec.and((root, cb) -> cb.isNotNull(buildPath(root, q)));
                        break;
                    }
                    spec = spec.and((root, cb) -> cb.notEqual(buildPath(root, q), q.value));
                    break;
                case LT, LE, GT, GE:
                    spec = spec.and(compare(q));
                    break;
                case CONTAINS:
                    spec = spec.and((root, cb) -> cb.like(buildPath(root, q), prepareLikeRegex(q), '!'));
                    break;
                case NCONTAINS:
                    spec = spec.and((root, cb) -> cb.notLike(buildPath(root, q), prepareLikeRegex(q), '!'));
                    break;
            }
        }

        return spec;
    }

    private String prepareLikeRegex(FilterQuery q) {
        String value = (String) q.value;
        value = value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + value + "%";
    }

    private <T> Path<T> buildPath(Root<Flat> root, FilterQuery q) {
        String[] pathParts = q.field.getName().split("\\.");

        Path<T> path = root.get(pathParts[0]);

        for (int i = 1; i < pathParts.length; i++) {
            path = path.get(pathParts[i]);
        }

        return path;
    }

    private PredicateSpecification<Flat> compare(FilterQuery q) {
        if (q.value instanceof Number number) {
            switch (q.operator) {
                case LT:
                    return (root, cb) -> cb.lt(buildPath(root, q), number);
                case LE:
                    return (root, cb) -> cb.le(buildPath(root, q), number);
                case GT:
                    return (root, cb) -> cb.gt(buildPath(root, q), number);
                case GE:
                    return (root, cb) -> cb.ge(buildPath(root, q), number);
                default:
                    throw new IllegalArgumentException("not comparable operator");
            }
        }

        if (q.value instanceof LocalDate date) {
            switch (q.operator) {
                case LT:
                    return (root, cb) -> cb.lessThan(buildPath(root, q), date);
                case LE:
                    return (root, cb) -> cb.lessThanOrEqualTo(buildPath(root, q), date);
                case GT:
                    return (root, cb) -> cb.greaterThan(buildPath(root, q), date);
                case GE:
                    return (root, cb) -> cb.greaterThanOrEqualTo(buildPath(root, q), date);

                default:
                    throw new IllegalArgumentException("not comparable operator");
            }
        }

        if (q.value instanceof Enum<?> enumValue) {
            return (root, cb) -> {
                var path = buildPath(root, q);
                var ranks = cb.<Integer>selectCase();

                for (var constanst : enumValue.getDeclaringClass().getEnumConstants()) {
                    ranks.when(cb.equal(path, constanst), constanst.ordinal());
                }

                var rank = ranks.otherwise(cb.nullLiteral(Integer.class));

                switch (q.operator) {
                    case LT:
                        return cb.lt(rank, enumValue.ordinal());
                    case LE:
                        return cb.le(rank, enumValue.ordinal());
                    case GT:
                        return cb.gt(rank, enumValue.ordinal());
                    case GE:
                        return cb.ge(rank, enumValue.ordinal());
                    default:
                        throw new IllegalArgumentException("not comparable operator");

                }
            };

        }

        throw new IllegalArgumentException("not comparable type");
    }

    private List<FilterQuery> parseFilterQuery(List<@NotNull String> filterQueries) {
        List<FilterQuery> parsed = new ArrayList<>();

        for (String filterQuery : filterQueries) {
            var queryParts = filterQuery.split("_", 3);
            if (queryParts.length != 3) {
                continue;
            }

            String fieldRaw = queryParts[0];
            String operatorRaw = queryParts[1];
            String valueRaw = queryParts[2];

            if (!FilterField.isFilterFieldAvailable(fieldRaw) ||
                    !FilterOperator.isFilterOperatorAvailable(operatorRaw)) {
                continue;
            }

            FilterField field = FilterField.of(fieldRaw);
            FilterOperator operator = FilterOperator.of(operatorRaw);

            if (!field.isOperatorAvailable(operator)) {
                continue;
            }

            Object value;
            try {
                if ("null".equals(valueRaw) && field.isNullable() && (operator == EQ || operator == NEQ)) {
                    value = null;
                } else {
                    value = conversionService.convertRequired(valueRaw, field.getFieldClass());
                }
            } catch (ConversionErrorException e) {
                continue;
            }

            parsed.add(new FilterQuery(field, operator, value));
        }

        return parsed;
    }

    private record FilterQuery(FilterField field, FilterOperator operator, Object value) {
    }
}
