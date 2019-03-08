package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class LocalTimeScalar extends GraphQLScalarType {

    public LocalTimeScalar() {
        super("LocalTime", "JDK8 LocalTime GraphQL ScalarType", new Coercing<LocalTime, String>() {
            @Override
            public String serialize(Object input) {
                Optional<LocalTime> localTime;

                if (input instanceof String) {
                    localTime = Optional.of(parseLocalTime(input.toString(), CoercingSerializeException::new));
                } else {
                    localTime = toLocalTime(input, CoercingSerializeException::new);
                }

                if (localTime.isPresent()) {
                    return DateTimeFormatter.ISO_LOCAL_TIME.format(localTime.get());
                }

                throw new CoercingSerializeException(
                        "Expected a 'String' or 'TemporalAccessor' but was '" + typeName(input) + "'."
                );
            }

            @Override
            public LocalTime parseValue(Object input) {
                if (input instanceof String) {
                    return parseLocalTime(String.valueOf(input), CoercingParseValueException::new);
                } else {
                    Optional<LocalTime> localTime = toLocalTime(input, CoercingParseValueException::new);
                    if (!localTime.isPresent()) {
                        throw new CoercingParseValueException(
                                "Expected a 'LocalTime' like object but was '" + typeName(input) + "'."
                        );
                    }
                    return localTime.get();
                }

            }

            @Override
            public LocalTime parseLiteral(Object input) {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    );
                }
                return parseLocalTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
            }
        });
    }


    private static LocalTime parseLocalTime(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply("Invalid RFC3339 local time value : '" + input + "'. because of : '" + e.getMessage() + "'");
        }
    }

    private static Optional<LocalTime> toLocalTime(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(LocalTime.from((TemporalAccessor) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
