package ru.ifmo.se.query;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.ifmo.se.exception.InvalidQueryException;
import ru.ifmo.se.model.Flat;

@Component
public class FlatSortBuilder {
    public Specification<Flat> build(List<String> parameters) {
        List<Sort.Order> sorting = parse(parameters);

        return (root, query, builder) -> {
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                List<Order> orders = new ArrayList<>();
                for (Sort.Order order : sorting) {
                    FlatField field = FlatField.require(order.getProperty());
                    Expression<?> expression = field.path(root);
                    if (field.nullable()) {
                        orders.add(builder.asc(builder.<Integer>selectCase()
                                .when(builder.isNull(expression), 1).otherwise(0)));
                    }
                    if (field.isEnum()) {
                        expression = field.enumOrder(builder, expression);
                    }
                    orders.add(order.isAscending() ? builder.asc(expression) : builder.desc(expression));
                }
                query.orderBy(orders);
            }
            return builder.conjunction();
        };
    }

    private List<Sort.Order> parse(List<String> parameters) {
        List<Sort.Order> sorting = new ArrayList<>();
        if (parameters != null) {
            for (String parameter : parameters) {
                if (parameter == null) {
                    throw new InvalidQueryException("sort", "Сортировка не может быть null");
                }
                for (String token : parameter.split(",", -1)) {
                    int separator = token.lastIndexOf('_');
                    if (separator <= 0) {
                        throw new InvalidQueryException("sort", "Ожидалась сортировка вида поле_asc или поле_desc");
                    }

                    FlatField field = FlatField.require(token.substring(0, separator));
                    if (field.isAssociation()) {
                        throw new InvalidQueryException("sort", "Укажите поле дома, например house.year");
                    }

                    String direction = token.substring(separator + 1);
                    if (!direction.equals("asc") && !direction.equals("desc")) {
                        throw new InvalidQueryException("sort", "Неизвестное направление сортировки: " + direction);
                    }
                    
                    sorting.add(new Sort.Order(Sort.Direction.fromString(direction), field.property()));
                }
            }
        }
        if (sorting.stream().noneMatch(order -> order.getProperty().equals("id"))) {
            sorting.add(Sort.Order.asc("id"));
        }
        return sorting;
    }
}
