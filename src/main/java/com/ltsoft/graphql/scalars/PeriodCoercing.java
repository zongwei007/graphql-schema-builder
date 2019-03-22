package com.ltsoft.graphql.scalars;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.time.DateTimeException;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class PeriodCoercing implements Coercing<Period, String> {

    @Override
    public String serialize(Object input) {
        Optional<Period> period;

        if (input instanceof String) {
            period = Optional.ofNullable(parsePeriod(input.toString(), CoercingSerializeException::new));
        } else {
            period = toPeriod(input, CoercingParseValueException::new);
        }

        if (period.isPresent()) {
            return period.get().toString();
        }

        throw new CoercingSerializeException(
                "Expected a 'String' or 'TemporalAmount' or 'Integer' but was '" + typeName(input) + "'."
        );
    }

    @Override
    public Period parseValue(Object input) {
        if (input instanceof String) {
            return parsePeriod(String.valueOf(input), CoercingParseValueException::new);
        } else {
            Optional<Period> period = toPeriod(input, CoercingParseValueException::new);
            if (!period.isPresent()) {
                throw new CoercingParseValueException(
                        "Expected a 'Period' like object but was '" + typeName(input) + "'."
                );
            }
            return period.get();
        }
    }

    @Override
    public Period parseLiteral(Object input) {
        if (input instanceof StringValue) {
            return parsePeriod(((StringValue) input).getValue(), CoercingParseLiteralException::new);
        }

        if (input instanceof IntValue) {
            return parsePeriod(((IntValue) input).getValue().intValue(), CoercingParseLiteralException::new);
        }

        throw new CoercingParseLiteralException(
                "Expected AST type 'StringValue' or 'IntValue' but was '" + typeName(input) + "'."
        );
    }

    private static Period parsePeriod(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof String) {
                return Period.parse((String) input);
            } else if (input instanceof Integer) {
                return Period.ofDays((Integer) input);
            } else {
                return null;
            }
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<Period> toPeriod(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAmount) {
                return Optional.of(Period.from((TemporalAmount) input));
            } else if (input instanceof Integer) {
                return Optional.of(Period.ofDays((Integer) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
