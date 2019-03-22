package com.ltsoft.graphql;

import com.google.common.reflect.ClassPath;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.DefaultServiceInstanceFactory;
import com.ltsoft.graphql.resolver.ResolveUtil;
import com.ltsoft.graphql.resolver.TypeDefinitionResolver;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class TypeDefinitionRegistryBuilder {

    private List<String> packageNames = new ArrayList<>();
    private ServiceInstanceFactory serviceInstanceFactory = new DefaultServiceInstanceFactory();

    public TypeDefinitionRegistryBuilder register(String... packages) {
        Collections.addAll(packageNames, packages);
        return this;
    }

    public TypeDefinitionRegistryBuilder setServiceInstanceFactory(ServiceInstanceFactory serviceInstanceFactory) {
        this.serviceInstanceFactory = serviceInstanceFactory;
        return this;
    }

    public TypeDefinitionRegistry build() {
        Document.Builder builder = Document.newDocument();
        TypeDefinitionResolver resolver = new TypeDefinitionResolver(serviceInstanceFactory);

        packageNames.stream()
                .flatMap(this::searchPackage)
                .map(cls -> loadTypeDefinition(cls, resolver))
                .forEach(builder::definition);

        return new SchemaParser().buildRegistry(builder.build());
    }

    private Definition<?> loadTypeDefinition(Class<?> cls, TypeDefinitionResolver resolver) {
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

    private Stream<? extends Class<?>> searchPackage(String packageName) {
        try {
            //noinspection UnstableApiUsage
            return ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(packageName).stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(ResolveUtil::canResolve);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Search package '%s' but fail", packageName), e);
        }
    }

}
