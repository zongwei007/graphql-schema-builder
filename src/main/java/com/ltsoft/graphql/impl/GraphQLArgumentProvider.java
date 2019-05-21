package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentConverter;
import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.annotations.GraphQLTypeReference;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Parameter;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public class GraphQLArgumentProvider implements ArgumentProvider<Object> {

    private final TypeToken<?> typeToken;
    private final String argumentName;
    private final List<ArgumentConverter<?>> converters;
    private boolean isGenericType;

    public GraphQLArgumentProvider(Class<?> cls, Parameter parameter, List<ArgumentConverter<?>> converters) {
        checkArgument(parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class));

        this.typeToken = resolveGenericType(cls, parameter.getParameterizedType());
        this.argumentName = resolveArgumentName(parameter);

        Class<?> rawType = typeToken.getRawType();
        //非 List 类型，且未声明类型引用
        if (!isGraphQLList(typeToken) && !parameter.isAnnotationPresent(GraphQLTypeReference.class)) {
            //非枚举且直接定义为 GraphQLType 的参数，才能识别为泛参数
            if (!rawType.isEnum() && rawType.isAnnotationPresent(GraphQLType.class)) {
                this.isGenericType = true;
            }
        }

        this.converters = converters;
    }

    @Override
    public Object provide(DataFetchingEnvironment environment) {
        if (isGenericType) {
            return convertTo(environment.getArguments(), typeToken);
        }

        return convertTo(environment.getArgument(requireNonNull(argumentName)), typeToken);
    }

    @SuppressWarnings("unchecked")
    private Object convertTo(Object argument, TypeToken<?> type) {
        if (argument == null) {
            return null;
        }

        TypeToken<Object> typeToken = (TypeToken<Object>) type;

        return converters.stream()
                .filter(ele -> ele.isSupport(TypeToken.of(argument.getClass()), type))
                .findFirst()
                .map(ele -> (ArgumentConverter<Object>) ele)
                .map(ele -> ele.convert(argument, typeToken, this::convertTo))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("can not cast '%s' to '%s'", argument.getClass().getSimpleName(), type.toString())
                ));
    }

    String getArgumentName() {
        return argumentName;
    }

    boolean isGenericType() {
        return isGenericType;
    }
}
