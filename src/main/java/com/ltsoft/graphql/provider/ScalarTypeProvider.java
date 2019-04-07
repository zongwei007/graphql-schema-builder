package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

public class ScalarTypeProvider implements TypeProvider<ScalarTypeDefinition> {

    private final GraphQLScalarType type;

    public ScalarTypeProvider(GraphQLScalarType type) {
        this.type = type;
    }

    @Override
    public Definition<ScalarTypeDefinition> getDefinition() {
        return new ScalarTypeDefinition(type.getName());
    }

    @Override
    public UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        return builder -> builder.scalar(type);
    }
}
