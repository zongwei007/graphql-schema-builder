package com.ltsoft.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class UriScalar extends GraphQLScalarType {
    public UriScalar() {
        super("URI", "URI GraphQLName", new Coercing<URI, URI>() {
            @Override
            public URI serialize(Object input) throws CoercingSerializeException {
                Optional<URI> uri;

                if (input instanceof String) {
                    uri = Optional.of(parseURI(input.toString(), CoercingSerializeException::new));
                } else {
                    uri = toURI(input, CoercingSerializeException::new);
                }

                if (uri.isPresent()) {
                    return uri.get();
                }

                throw new CoercingSerializeException(
                        "Expected a 'URI' like object but was '" + typeName(input) + "'."
                );
            }

            @Override
            public URI parseValue(Object input) throws CoercingParseValueException {
                if (input instanceof String) {
                    return parseURI(String.valueOf(input), CoercingParseValueException::new);
                } else {
                    Optional<URI> uri = toURI(input, CoercingParseValueException::new);
                    if (!uri.isPresent()) {
                        throw new CoercingParseValueException(
                                "Expected a 'URI' like object but was '" + typeName(input) + "'."
                        );
                    }
                    return uri.get();
                }
            }

            @Override
            public URI parseLiteral(Object input) throws CoercingParseLiteralException {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    );
                }
                return parseURI(((StringValue) input).getValue(), CoercingParseLiteralException::new);
            }
        });
    }

    private static URI parseURI(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return new URI(input);
        } catch (URISyntaxException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<URI> toURI(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof URI) {
                return Optional.of((URI) input);
            } else if (input instanceof URL) {
                return Optional.of(((URL) input).toURI());
            }
        } catch (URISyntaxException | SecurityException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }
}
