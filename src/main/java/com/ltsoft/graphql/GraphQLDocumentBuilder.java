package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.resolver.DefinitionResolver;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.schema.GraphQLScalarType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public final class GraphQLDocumentBuilder {

    private final Set<Class<?>> types = new HashSet<>();
    private final DefinitionResolver resolver = new DefinitionResolver();

    public GraphQLDocumentBuilder addScalar(GraphQLScalarType scalarType, Class<?> javaType) {
        resolver.scalar(scalarType, javaType);
        return this;
    }

    public GraphQLDocumentBuilder withTypeResolver(TypeResolver<?>... typeResolvers) {
        resolver.typeResolver(typeResolvers);
        return this;
    }

    public GraphQLDocumentBuilder withType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public Set<GraphQLScalarType> getAllExtensionScalars() {
        return resolver.getTypeRepository().allExtensionTypes();
    }

    public Document.Builder builder() {
        Document.Builder builder = Document.newDocument();

        types.stream().map(cls -> loadTypeDefinition(cls, resolver))
                .forEach(builder::definition);
        resolver.getTypeRepository().allExtensionTypeDefinitions().forEach(builder::definition);
        resolver.getTypeDefinitionExtensions().forEach(builder::definition);

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

        if (cls.isAnnotationPresent(GraphQLDirective.class)) {
            return resolver.directive(cls);
        }

        if (cls.isAnnotationPresent(GraphQLUnion.class)) {
            return resolver.union(cls);
        }

        throw new IllegalArgumentException(String.format("Unknown java type: '%s'", cls.getName()));
    }


}
