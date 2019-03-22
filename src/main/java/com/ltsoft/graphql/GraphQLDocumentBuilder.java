package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.resolver.DefinitionResolver;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.schema.GraphQLScalarType;

import java.util.*;

public final class GraphQLDocumentBuilder {

    private final Set<Class<?>> types = new HashSet<>();
    private final ServiceInstanceFactory serviceInstanceFactory;
    private final Map<GraphQLScalarType, Class<?>> scalarTypeMap = new HashMap<>();

    public GraphQLDocumentBuilder(ServiceInstanceFactory serviceInstanceFactory) {
        this.serviceInstanceFactory = serviceInstanceFactory;
    }

    public GraphQLDocumentBuilder addScalar(GraphQLScalarType scalarType, Class<?> javaType) {
        scalarTypeMap.put(scalarType, javaType);
        return this;
    }

    public GraphQLDocumentBuilder withType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public Document.Builder builder() {
        Document.Builder builder = Document.newDocument();
        DefinitionResolver resolver = new DefinitionResolver(serviceInstanceFactory);

        scalarTypeMap.forEach(resolver::scalar);

        types.stream().map(cls -> loadTypeDefinition(cls, resolver))
                .forEach(builder::definition);

        return builder;
    }

    private Definition<?> loadTypeDefinition(Class<?> cls, DefinitionResolver resolver) {
        if (cls.isAnnotationPresent(GraphQLType.class)) {
            if (cls.isEnum()) {
                return resolver.enumeration(cls);
            } else {
                return resolver.object(cls);
            }
        }

        if (cls.isAnnotationPresent(GraphQLInterface.class)) {
            return resolver.iface(cls);
        }

        if (cls.isAnnotationPresent(GraphQLInput.class)) {
            return resolver.input(cls);
        }

        if (cls.isAnnotationPresent(GraphQLTypeExtension.class)) {
            return resolver.extension(cls);
        }

        if (cls.isAnnotationPresent(GraphQLDirectiveLocations.class)) {
            return resolver.directive(cls);
        }

        if (cls.isAnnotationPresent(GraphQLUnion.class)) {
            return resolver.union(cls);
        }

        throw new IllegalArgumentException(String.format("Unknown java type: '%s'", cls.getName()));
    }


}
