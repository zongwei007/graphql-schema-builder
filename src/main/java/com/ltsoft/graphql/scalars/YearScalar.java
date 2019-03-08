package com.ltsoft.graphql.scalars;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class YearScalar extends GraphQLScalarType {

    public YearScalar() {
        super("Year", "JDK8 Year GraphQLName", new Coercing<Year, Year>() {
            @Override
            public Year serialize(Object input) {
                Optional<Year> year;

                if (input instanceof String) {
                    year = Optional.of(parseYear(input.toString(), CoercingSerializeException::new));
                } else {
                    year = toYear(input, CoercingSerializeException::new);
                }

                if (year.isPresent()) {
                    return year.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'Year' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public Year parseValue(Object input) {
                if (input instanceof String) {
                    return parseYear(String.valueOf(input), CoercingParseValueException::new);
                }

                Optional<Year> year = toYear(input, CoercingParseValueException::new);
                if (!year.isPresent()) {
                    throw new CoercingParseValueException(
                            "Expected a 'Year' like object but was '" + typeName(input) + "'."
                    );
                }
                return year.get();
            }

            @Override
            public Year parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    return parseYear(((StringValue) input).getValue(), CoercingParseLiteralException::new);
                }

                if (input instanceof IntValue) {
                    return parseYear(String.valueOf(((IntValue) input).getValue().intValue()), CoercingParseLiteralException::new);
                }

                throw new CoercingParseLiteralException(
                        "Expected AST type 'StringValue' or 'IntValue' but was '" + typeName(input) + "'."
                );
            }
        });
    }

    private static Year parseYear(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return Year.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<Year> toYear(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(Year.from((TemporalAccessor) input));
            } else if (input instanceof Integer) {
                return Optional.of(Year.of((Integer) input));
            }
        } catch (DateTimeException e) {
            exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
