package com.ltsoft.graphql.example;

import graphql.schema.*;

public class HelloObjectScalar extends GraphQLScalarType {
    public HelloObjectScalar() {
        super("Hello", "Hello Scalar Type", new Coercing<HelloObject, String>() {

            @Override
            public String serialize(Object result) throws CoercingSerializeException {
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
        });
    }
}
