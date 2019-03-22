package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class ZonedDateTimeCoercing implements Coercing<ZonedDateTime, ZonedDateTime> {

    @Override
    public ZonedDateTime serialize(Object input) {
        Optional<ZonedDateTime> zonedDateTime;

        if (input instanceof String) {
            zonedDateTime = Optional.of(parseZonedDateTime(input.toString(), CoercingSerializeException::new));
        } else {
            zonedDateTime = toZonedDateTime(input, CoercingSerializeException::new);
        }

        if (zonedDateTime.isPresent()) {
            return zonedDateTime.get();
        }

        throw new CoercingSerializeException(
                "Expected a 'ZonedDateTime' like object but was '" + typeName(input) + "'."
        );
    }

    @Override
    public ZonedDateTime parseValue(Object input) {
        if (input instanceof String) {
            return parseZonedDateTime(String.valueOf(input), CoercingParseValueException::new);
        }

        Optional<ZonedDateTime> zonedDateTime = toZonedDateTime(input, CoercingParseValueException::new);
        if (!zonedDateTime.isPresent()) {
            throw new CoercingParseValueException(
                    "Expected a 'ZonedDateTime' like object but was '" + typeName(input) + "'."
            );
        }
        return zonedDateTime.get();
    }

    @Override
    public ZonedDateTime parseLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
            );
        }
        return parseZonedDateTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
    }


    private static ZonedDateTime parseZonedDateTime(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return ZonedDateTime.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<ZonedDateTime> toZonedDateTime(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(ZonedDateTime.from((TemporalAccessor) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
