package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class LocalDateTimeScalar extends GraphQLScalarType {

    public LocalDateTimeScalar() {
        super("LocalDateTime", "JDK8 LocalDateTime GraphQLName", new Coercing<LocalDateTime, LocalDateTime>() {
            @Override
            public LocalDateTime serialize(Object input) {
                Optional<LocalDateTime> localDateTime;

                if (input instanceof String) {
                    localDateTime = Optional.of(parseLocalDateTime(input.toString(), CoercingSerializeException::new));
                } else {
                    localDateTime = toLocalDateTime(input, CoercingSerializeException::new);
                }

                if (localDateTime.isPresent()) {
                    return localDateTime.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'LocalDateTime' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public LocalDateTime parseValue(Object input) {
                if (input instanceof String) {
                    return parseLocalDateTime(String.valueOf(input), CoercingParseValueException::new);
                }

                Optional<LocalDateTime> localDateTime = toLocalDateTime(input, CoercingParseValueException::new);
                if (!localDateTime.isPresent()) {
                    throw new CoercingParseValueException(
                            "Expected a 'LocalDateTime' like object but was '" + typeName(input) + "'."
                    );
                }

                return localDateTime.get();
            }

            @Override
            public LocalDateTime parseLiteral(Object input) {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    );
                }
                return parseLocalDateTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
            }
        });
    }

    private static LocalDateTime parseLocalDateTime(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<LocalDateTime> toLocalDateTime(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(LocalDateTime.from((TemporalAccessor) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
