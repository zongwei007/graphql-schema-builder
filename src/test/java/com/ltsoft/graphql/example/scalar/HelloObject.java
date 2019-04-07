package com.ltsoft.graphql.example.scalar;

import graphql.schema.*;

@SuppressWarnings("WeakerAccess")
public class HelloObject {

    public static GraphQLScalarType HelloObjectScalar = GraphQLScalarType.newScalar()
            .name("Hello")
            .description("Hello Scalar Type")
            .coercing(new Coercing<HelloObject, String>() {

                @Override
                public String serialize(Object result) throws CoercingSerializeException {
                    if (result instanceof String) {
                        return (String) result;
                    }
                    return ((HelloObject) result).getValue();
                }

                @Override
                public HelloObject parseValue(Object input) throws CoercingParseValueException {
                    return new HelloObject(String.valueOf(input));
                }

                @Override
                public HelloObject parseLiteral(Object input) throws CoercingParseLiteralException {
                    return parseValue(input);
                }
            })
            .build();

    private String value;

    public HelloObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
