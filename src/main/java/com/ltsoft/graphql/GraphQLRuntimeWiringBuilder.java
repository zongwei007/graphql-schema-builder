package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.resolver.EnumFieldValueProvider;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;

import java.util.*;

public final class GraphQLRuntimeWiringBuilder {

    private final Set<GraphQLScalarType> scalarTypeSet = new HashSet<>();
    private final Map<String, EnumFieldValueProvider> enumValueProviders = new HashMap<>();

    public GraphQLRuntimeWiringBuilder withScalar(GraphQLScalarType... scalarTypes) {
        Collections.addAll(scalarTypeSet, scalarTypes);
        return this;
    }

    public GraphQLRuntimeWiringBuilder withType(Class<?>... classes) {
        Arrays.stream(classes)
                .filter(Class::isEnum)
                .filter(cls -> cls.isAnnotationPresent(GraphQLType.class) || cls.isAnnotationPresent(GraphQLTypeExtension.class))
                .map(EnumFieldValueProvider::new)
                .forEach(provider ->
                        enumValueProviders.computeIfAbsent(provider.getTypeName(), key -> provider).merge(provider)
                );

        return this;
    }

    public RuntimeWiring.Builder builder() {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        scalarTypeSet.forEach(builder::scalar);
        enumValueProviders.forEach((typeName, provider) -> builder.type(typeName, e -> e.enumValues(provider)));

        return builder;
    }

}
