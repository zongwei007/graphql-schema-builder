package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class UuidCoercing implements Coercing<UUID, UUID> {

    @Override
    public UUID serialize(Object input) {
        Optional<UUID> uuid;

        if (input instanceof String) {
            uuid = Optional.of(parseUUID(input.toString(), CoercingSerializeException::new));
        } else {
            uuid = toUUID(input);
        }

        if (uuid.isPresent()) {
            return uuid.get();
        }

        throw new CoercingSerializeException(
                "Expected a 'UUID' like object but was '" + typeName(input) + "'."
        );
    }

    @Override
    public UUID parseValue(Object input) {
        if (input instanceof String) {
            return parseUUID(String.valueOf(input), CoercingParseValueException::new);
        } else {
            Optional<UUID> uuid = toUUID(input);
            if (!uuid.isPresent()) {
                throw new CoercingParseValueException(
                        "Expected a 'UUID' like object but was '" + typeName(input) + "'."
                );
            }
            return uuid.get();
        }
    }

    @Override
    public UUID parseLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
            );
        }

        return parseUUID(((StringValue) input).getValue(), CoercingParseLiteralException::new);
    }

    private static UUID parseUUID(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<UUID> toUUID(Object input) {
        if (input instanceof UUID) {
            return Optional.of((UUID) input);
        }

        return Optional.empty();
    }
}
