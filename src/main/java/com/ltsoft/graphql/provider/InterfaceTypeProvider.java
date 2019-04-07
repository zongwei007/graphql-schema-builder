package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;
import graphql.language.InterfaceTypeDefinition;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

public class InterfaceTypeProvider implements TypeProvider<InterfaceTypeDefinition> {

    private final InterfaceTypeDefinition definition;
    private final TypeResolver typeResolver;

    public InterfaceTypeProvider(InterfaceTypeDefinition definition, TypeResolver typeResolver) {
        this.definition = definition;
        this.typeResolver = typeResolver;
    }

    @Override
    public Definition<InterfaceTypeDefinition> getDefinition() {
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
