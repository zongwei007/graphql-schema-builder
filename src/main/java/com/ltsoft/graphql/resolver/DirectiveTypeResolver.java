package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.GraphQLDirective;
import com.ltsoft.graphql.annotations.GraphQLDirectiveLocations;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ltsoft.graphql.resolver.ResolveUtil.*;

public class DirectiveTypeResolver extends BasicTypeResolver<DirectiveDefinition> {

    private static Map<ElementType, Introspection.DirectiveLocation> LOCATION_MAP = new HashMap<>();

    static {
        LOCATION_MAP.put(ElementType.TYPE, Introspection.DirectiveLocation.OBJECT);
        LOCATION_MAP.put(ElementType.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        LOCATION_MAP.put(ElementType.METHOD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        LOCATION_MAP.put(ElementType.PARAMETER, Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
    }

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        return TypeToken.of(javaType).getRawType().isAnnotationPresent(GraphQLDirective.class);
    }

    @Override
    TypeProvider<DirectiveDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        DirectiveDefinition definition = DirectiveDefinition.newDirectiveDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directiveLocations(resolveDirectiveLocation(cls))
                .inputValueDefinitions(resolveInputValueDefinitions(cls, resolver))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();

        return () -> definition;
    }

    private List<InputValueDefinition> resolveInputValueDefinitions(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        return resolveFields(cls)
                .filter(this::isDirectiveArgument)
                .filter(FieldInformation::isNotIgnore)
                .map(info -> info.getInputValueDefinition(resolver))
                .collect(Collectors.toList());
    }

    private List<DirectiveLocation> resolveDirectiveLocation(Class<?> cls) {
        Stream<Introspection.DirectiveLocation> locationStream = Stream.of();

        if (cls.isAnnotationPresent(GraphQLDirectiveLocations.class)) {
            locationStream = Arrays.stream(cls.getAnnotation(GraphQLDirectiveLocations.class).value());
        } else if (cls.isAnnotationPresent(Target.class)) {
            locationStream = Arrays.stream(cls.getAnnotation(Target.class).value())
                    .map(ele -> LOCATION_MAP.get(ele))
                    .filter(Objects::nonNull);
        }

        return locationStream.map(ele -> new DirectiveLocation(ele.name())).collect(Collectors.toList());
    }

    private boolean isDirectiveArgument(FieldInformation info) {
        return info.test((method, field) -> isSupport(method.getDeclaringClass()));
    }
}
