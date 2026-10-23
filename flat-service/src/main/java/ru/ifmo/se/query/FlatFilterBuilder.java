package ru.ifmo.se.query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.ifmo.se.exception.InvalidQueryException;
import ru.ifmo.se.model.Flat;

@Component
public class FlatFilterBuilder {
    private static final Set<String> OPERATORS = Set.of("eq", "neq", "gt", "ge", "lt", "le", "contains", "ncontains");

    public Specification<Flat> build(List<String> filters) {
        List<Specification<Flat>> specifications = new ArrayList<>();
        if (filters != null) {
            for (String filter : filters) {
                specifications.add(parse(filter));
            }
        }
        return Specification.allOf(specifications);
    }

    private Specification<Flat> parse(String filter) {
        if (filter == null) {
            throw new InvalidQueryException("filter", "Фильтр не может быть null");
        }

        String[] parts = filter.split("_", 3);
        if (parts.length != 3 || !OPERATORS.contains(parts[1])) {
            throw new InvalidQueryException("filter", "Ожидался фильтр вида поле_оператор_значение: " + filter);
        }

        FlatField field = FlatField.require(parts[0]);
        String operator = parts[1];
        String rawValue = parts[2];

        if (field.nullable() && rawValue.equals("null") && (operator.equals("eq") || operator.equals("neq"))) {
            return (root, query, builder) -> operator.equals("eq")
                    ? builder.isNull(field.path(root)) : builder.isNotNull(field.path(root));
        }

        if ((operator.equals("contains") || operator.equals("ncontains")) && !field.isString()) {
            throw new InvalidQueryException(field.property(), "Оператор " + operator + " применим только к строкам");
        }

        Object value = field.parse(rawValue);
        return (root, query, builder) -> {
            Expression<?> expression = field.path(root);
            return switch (operator) {
                case "eq" -> builder.equal(expression, value);
                case "neq" -> builder.notEqual(expression, value);
                case "contains" -> builder.like(expression.as(String.class), containsPattern(rawValue), '!');
                case "ncontains" -> builder.notLike(expression.as(String.class), containsPattern(rawValue), '!');
                default -> field.isEnum()
                        ? builder.and(builder.isNotNull(expression),
                                compare(builder, field.enumOrder(builder, expression), ((Enum<?>) value).ordinal(), operator))
                        : compare(builder, expression, (Comparable<?>) value, operator);
            };
        };
    }

    private String containsPattern(String value) {
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate compare(CriteriaBuilder builder, Expression<?> expression, Comparable<?> value, String operator) {
        return switch (operator) {
            case "gt" -> builder.greaterThan((Expression) expression, (Comparable) value);
            case "ge" -> builder.greaterThanOrEqualTo((Expression) expression, (Comparable) value);
            case "lt" -> builder.lessThan((Expression) expression, (Comparable) value);
            case "le" -> builder.lessThanOrEqualTo((Expression) expression, (Comparable) value);
            default -> throw new InvalidQueryException("filter", "Неизвестный оператор: " + operator);
        };
    }
}
