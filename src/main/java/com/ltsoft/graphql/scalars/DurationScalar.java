package com.ltsoft.graphql.scalars;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class DurationScalar extends GraphQLScalarType {

    public DurationScalar() {
        super("Duration", "JDK8 Duration GraphQLName", new Coercing<Duration, Duration>() {
            @Override
            public Duration serialize(Object input) {
                Optional<Duration> duration;

                if (input instanceof String) {
                    duration = Optional.ofNullable(parseDuration(input.toString(), CoercingSerializeException::new));
                } else {
                    duration = toDuration(input, CoercingSerializeException::new);
                }

                if (duration.isPresent()) {
                    return duration.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'Duration' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public Duration parseValue(Object input) {
                if (input instanceof String) {
                    return parseDuration(input, CoercingParseValueException::new);
                }

                Optional<Duration> duration = toDuration(input, CoercingParseValueException::new);
                if (!duration.isPresent()) {
                    throw new CoercingParseValueException(
                            "Expected a 'Duration' like object but was '" + typeName(input) + "'."
                    );
                }
                return duration.get();
            }

            @Override
            public Duration parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    return parseDuration(((StringValue) input).getValue(), CoercingParseLiteralException::new);
                }

                if (input instanceof IntValue) {
                    return parseDuration(((IntValue) input).getValue().longValue(), CoercingParseLiteralException::new);
                }

                throw new CoercingParseLiteralException(
                        "Expected AST type 'StringValue' or 'IntValue' but was '" + typeName(input) + "'."
                );
            }
        });
    }

    private static Duration parseDuration(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof String) {
                return Duration.parse((String) input);
            } else if (input instanceof Long) {
                return Duration.ofSeconds((Long) input);
            } else {
                return null;
            }
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<Duration> toDuration(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAmount) {
                return Optional.of(Duration.from((TemporalAmount) input));
            } else if (input instanceof Integer) {
                return Optional.of(Duration.ofSeconds(((Integer) input).longValue()));
            } else if (input instanceof Long) {
                return Optional.of(Duration.ofSeconds((Long) input));
            }
        } catch (ArithmeticException | DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
