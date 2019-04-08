package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.GraphQLInterface;
import com.ltsoft.graphql.provider.InterfaceTypeProvider;
import graphql.language.InterfaceTypeDefinition;
import graphql.schema.TypeResolver;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import static com.ltsoft.graphql.resolver.ResolveUtil.hasGraphQLAnnotation;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class InterfaceTypeResolver extends BasicTypeResolver<InterfaceTypeDefinition> {

    private InstanceFactory instanceFactory;

    InterfaceTypeResolver(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        return hasGraphQLAnnotation(TypeToken.of(javaType).getRawType(), GraphQLInterface.class);
    }

    @Override
    TypeProvider<InterfaceTypeDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        TypeResolver typeResolver = Optional.ofNullable(cls.getAnnotation(GraphQLInterface.class))
                .map(GraphQLInterface::typeResolver)
                .map(ele -> instanceFactory.provide(ele))
                .orElse(null);

        InterfaceTypeDefinition definition = InterfaceTypeDefinition.newInterfaceTypeDefinition()
                .comments(getComment(cls))
                .description(getDescription(cls))
                .definitions(getFieldDefinitions(cls, this::isInterfaceField, resolver))
                .directives(getDirective(cls, resolver))
                .name(resolveTypeName(cls))
                .sourceLocation(getSourceLocation(cls))
                .build();

        return new InterfaceTypeProvider(definition, typeResolver);
    }

    private boolean isInterfaceField(FieldInformation info) {
        return isSupport(info.getDeclaringClass());
    }
}
