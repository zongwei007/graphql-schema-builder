package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.GraphQLInput;
import com.ltsoft.graphql.annotations.GraphQLType;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.*;

public class InputObjectTypeResolver extends BasicTypeResolver<InputObjectTypeDefinition> {

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        return hasGraphQLAnnotation(TypeToken.of(javaType).getRawType(), GraphQLInput.class);
    }

    @Override
    TypeProvider<InputObjectTypeDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        InputObjectTypeDefinition definition = InputObjectTypeDefinition.newInputObjectDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls, resolver))
                .inputValueDefinitions(resolveInputValueDefinitions(cls, resolver))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();

        return () -> definition;
    }

    private List<InputValueDefinition> resolveInputValueDefinitions(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        return resolveFields(cls)
                .filter(FieldInformation::isSetter)
                .filter(FieldInformation::isNotIgnore)
                .filter(this::isInputField)
                .map(info -> info.getInputValueDefinition(new Class[0], resolver))
                .collect(Collectors.toList());
    }

    private boolean isInputField(FieldInformation info) {
        return info.test((method, field) ->
                hasGraphQLAnnotation(method.getDeclaringClass(), GraphQLInput.class) || hasGraphQLAnnotation(method.getDeclaringClass(), GraphQLType.class)
        );
    }
}
