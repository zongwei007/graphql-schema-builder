package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.GraphQLDefaultValue;
import com.ltsoft.graphql.annotations.GraphQLNotNull;
import com.ltsoft.graphql.annotations.GraphQLTypeReference;
import graphql.language.*;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.*;

class ArgumentInformation {

    private final FieldInformation field;
    private final Parameter parameter;
    private final Class<?>[] views;

    ArgumentInformation(FieldInformation field, Parameter parameter, Class<?>[] views) {
        this.field = field;
        this.parameter = parameter;
        this.views = views;
    }

    public boolean isNotNull() {
        return Optional.ofNullable(parameter.getAnnotation(GraphQLNotNull.class))
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    @SuppressWarnings("UnstableApiUsage")
    public Type getType(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        GraphQLTypeReference typeReference = parameter.getAnnotation(GraphQLTypeReference.class);
        TypeToken<?> paramType = resolveGenericType(field.getType(), parameter.getParameterizedType());

        Type type = null;

        if (isGraphQLInputType(unwrapListType(paramType).getRawType()) || typeReference != null) {
            type = resolveGraphQLType(paramType, resolver, isNotNull());
        }

        if (typeReference != null) {
            type = replaceTypeName(type, resolveTypeReference(typeReference, resolver));
        }

        return type;
    }

    @SuppressWarnings("UnstableApiUsage")
    public List<InputValueDefinition> getDefinitions(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Type inputType = getType(resolver);

        if (inputType != null) {
            InputValueDefinition argument = InputValueDefinition.newInputValueDefinition()
                    .description(getDescription())
                    .defaultValue(getDefaultValue(inputType))
                    .directives(getDirectives())
                    .name(getName())
                    .type(inputType)
                    .build();

            return Collections.singletonList(argument);
        } else if (!isGraphQLList(TypeToken.of(parameter.getParameterizedType()))) {
            //泛参数解析。将输入参数无法识别为 GraphQL Input 的 Java 对象的 Field 参数进行拆解，转换为一组 Input Value
            return resolveFields(parameter.getType())
                    .filter(FieldInformation::isSetter)
                    .filter(this::isGraphQLType)
                    .filter(this::isNotIgnore)
                    .map(info -> info.getInputValueDefinition(views, resolver))
                    .collect(Collectors.toList());
        }

        throw new IllegalArgumentException(String.format("Can not resolve GraphQL Input Type witch parameter %s of method %s#%s", parameter.getName(), field.getType().getName(), field.getMethod().getName()));
    }

    private boolean isNotIgnore(FieldInformation info) {
        return info.isNotIgnore(views);
    }

    private boolean isGraphQLType(FieldInformation info) {
        return info.test((method, field) -> method.getDeclaringClass().isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class));
    }

    private Value getDefaultValue(Type inputType) {
        //noinspection UnstableApiUsage
        TypeToken<?> unwrapType = unwrapListType(TypeToken.of(parameter.getType()));
        boolean isEnum = isGraphQLEnumType(unwrapType.getRawType());

        return resolveInputDefaultValue(parameter.getAnnotation(GraphQLDefaultValue.class), inputType, isEnum);
    }

    private List<Directive> getDirectives() {
        return resolveDirective(parameter);
    }

    private String getName() {
        return resolveArgumentName(parameter);
    }

    private Description getDescription() {
        return resolveDescription(parameter);
    }
}
