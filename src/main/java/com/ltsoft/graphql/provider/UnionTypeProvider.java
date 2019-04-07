package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;
import graphql.language.UnionTypeDefinition;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

public class UnionTypeProvider implements TypeProvider<UnionTypeDefinition> {
    private final UnionTypeDefinition definition;
    private final TypeResolver typeResolver;

    public UnionTypeProvider(UnionTypeDefinition definition, TypeResolver typeResolver) {
        this.definition = definition;
        this.typeResolver = typeResolver;
    }

    @Override
    public Definition<UnionTypeDefinition> getDefinition() {
        return definition;
    }

    @Override
    public UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        if (typeResolver != null) {
            return builder -> builder.type(getTypeName(), type -> type.typeResolver(typeResolver));
        } else {
            return UnaryOperator.identity();
        }
    }
}
