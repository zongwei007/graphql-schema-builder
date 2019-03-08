package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class YearMonthScalar extends GraphQLScalarType {

    public YearMonthScalar() {
        super("YearMonth", "JDK8 YearMonth GraphQLName", new Coercing<YearMonth, YearMonth>() {
            @Override
            public YearMonth serialize(Object input) {
                Optional<YearMonth> yearMonth;

                if (input instanceof String) {
                    yearMonth = Optional.of(parseYearMonth(input.toString(), CoercingSerializeException::new));
                } else {
                    yearMonth = toYearMonth(input, CoercingSerializeException::new);
                }

                if (yearMonth.isPresent()) {
                    return yearMonth.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'YearMonth' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public YearMonth parseValue(Object input) {
                if (input instanceof String) {
                    return parseYearMonth(String.valueOf(input), CoercingParseValueException::new);
                } else {
                    Optional<YearMonth> yearMonth = toYearMonth(input, CoercingParseValueException::new);
                    if (!yearMonth.isPresent()) {
                        throw new CoercingParseValueException(
                                "Expected a 'YearMonth' like object but was '" + typeName(input) + "'."
                        );
                    }
                    return yearMonth.get();
                }
            }

            @Override
            public YearMonth parseLiteral(Object input) {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    );
                }
                return parseYearMonth(((StringValue) input).getValue(), CoercingParseLiteralException::new);
            }
        });
    }

    private static YearMonth parseYearMonth(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return YearMonth.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<YearMonth> toYearMonth(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(YearMonth.from((TemporalAccessor) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
