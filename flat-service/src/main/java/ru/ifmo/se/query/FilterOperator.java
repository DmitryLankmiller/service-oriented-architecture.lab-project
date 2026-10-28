package ru.ifmo.se.query;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FilterOperator {
    EQ("eq"),
    NEQ("neq"),
    GT("gt"),
    GE("ge"),
    LT("lt"),
    LE("le"),
    CONTAINS("contains"),
    NCONTAINS("ncontains");

    public static final Set<FilterOperator> COMPARISON_OPERATORS = Set.of(EQ, NEQ, GT, GE, LT, LE);
    public static final Set<FilterOperator> STRING_OPERATORS = Set.of(EQ, NEQ, CONTAINS, NCONTAINS);

    private static final Set<String> AVAILABLE_OPERATORS = Stream.of(FilterOperator.values())
            .map(o -> o.getOperator())
            .collect(Collectors.toSet());

    private final String operator;

    FilterOperator(String operator) {
        this.operator = operator;
    }

    public static FilterOperator of(String operator) {
        for (var o : FilterOperator.values()) {
            if (o.operator.equals(operator)) {
                return o;
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean isFilterOperatorAvailable(String op) {
        return AVAILABLE_OPERATORS.contains(op);
    }

    public String getOperator() {
        return operator;
    }
}
