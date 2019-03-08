package com.ltsoft.graphql.scalars;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class InstantScalar extends GraphQLScalarType {

    public InstantScalar() {
        super("Instant", "JDK8 Instant GraphQLName", new Coercing<Instant, Instant>() {
            @Override
            public Instant serialize(Object input) {
                Optional<Instant> instant;

                if (input instanceof String) {
                    instant = Optional.ofNullable(parseInstant(input.toString(), CoercingSerializeException::new));
                } else {
                    instant = toInstant(input, CoercingSerializeException::new);
                }

                if (instant.isPresent()) {
                    return instant.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'Instant' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public Instant parseValue(Object input) {
                if (input instanceof String) {
                    return parseInstant(String.valueOf(input), CoercingParseValueException::new);
                }

                Optional<Instant> instant = toInstant(input, CoercingParseValueException::new);
                if (!instant.isPresent()) {
                    throw new CoercingParseValueException(
                            "Expected a 'Instant' like object but was '" + typeName(input) + "'."
                    );
                }
                return instant.get();
            }

            @Override
            public Instant parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    return parseInstant(((StringValue) input).getValue(), CoercingParseLiteralException::new);
                }

                if (input instanceof IntValue) {
                    return parseInstant(((IntValue) input).getValue().longValue(), CoercingParseLiteralException::new);
                }

                throw new CoercingParseLiteralException(
                        "Expected AST type 'StringValue' or 'IntValue' but was '" + typeName(input) + "'."
                );
            }
        });
    }

    private static Instant parseInstant(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof String) {
                return Instant.parse((String) input);
            } else if (input instanceof Long) {
                return Instant.ofEpochSecond((Long) input);
            } else {
                return null;
            }
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<Instant> toInstant(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(Instant.from((TemporalAccessor) input));
            } else if (input instanceof Long) {
                return Optional.of(Instant.ofEpochSecond((Long) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
