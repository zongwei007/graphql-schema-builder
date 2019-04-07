package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

public class ExtensionTypeProvider<T extends Definition> implements TypeProvider<T> {
    private final Definition<T> definition;
    private final TypeProvider<T> provider;
    private final TypeProvider<?> parentProvider;

    public ExtensionTypeProvider(Definition<T> definition, TypeProvider<T> provider, TypeProvider<?> parentProvider) {
        this.definition = definition;
        this.provider = provider;
        this.parentProvider = parentProvider;
    }

    @Override
    public Definition<T> getDefinition() {
        return definition;
    }

    @Override
    public UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        UnaryOperator<RuntimeWiring.Builder> operator = provider.getWiringOperator();
        UnaryOperator<RuntimeWiring.Builder> parentOperator = parentProvider.getWiringOperator();

        return builder -> operator.apply(parentOperator.apply(builder));
    }
}
